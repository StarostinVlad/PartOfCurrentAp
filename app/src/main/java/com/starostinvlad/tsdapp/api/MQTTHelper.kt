package com.starostinvlad.tsdapp.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.starostinvlad.tsdapp.data.AttachResult
import com.starostinvlad.tsdapp.data.MqttRpcCommand
import com.starostinvlad.tsdapp.data.MqttRpcCommandMethod
import kotlinx.coroutines.suspendCancellableCoroutine
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MQTTHelper @Inject constructor(
    private val context: Context,
    private val gson: Gson
) {
    private var request: Int = 0
    private var topic = "v1/devices/me/rpc/request/0"
        get() = "v1/devices/me/rpc/request/$request"
    private var lastTopic = ""
    private val listeners: MutableList<MqttMessageListener> = mutableListOf()
    private val TAG = javaClass.simpleName
    private lateinit var mqttClient: MqttAndroidClient
    var username: String? = null

    fun init(mqttHost: String, mqttPort: Int) {
        var host = mqttHost
        if (mqttHost.contains(":"))
            host = mqttHost.split(":")[0]
        val serverURI = "tcp://${host}:${mqttPort}"
        mqttClient = MqttAndroidClient(context, serverURI, "kotlin_client")

        mqttClient.setCallback(
            object : MqttCallback {
                override fun messageArrived(
                    topic: String?,
                    message: MqttMessage?
                ) {
                    if (topic != lastTopic) {
                        Log.d(
                            TAG,
                            "Receive message: ${message.toString()} from topic: $topic, listeners:${listeners.size}"
                        )
                        val result = gson.fromJson(
                            message?.toString(),
                            AttachResult::class.java
                        )
                        listeners.forEach {
                            it.messageArrived(result, topic)
                        }
                        lastTopic = topic!!
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    Log.d(TAG, "Connection lost ${cause.toString()}")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {

                }
            }
        )
    }

    fun addListener(listener: MqttMessageListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MqttMessageListener) {
        listeners.remove(listener)
    }

    suspend fun connect(mqttToken: String) =
        suspendCancellableCoroutine<Boolean> {
            val options = MqttConnectOptions()
            options.userName = mqttToken
            Log.d(TAG, "connect: $options.")
            try {
                mqttClient.connect(options, context, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "Connection success")
                        mqttClient.subscribe("v1/devices/me/rpc/response/+", 0)
                        mqttClient.subscribe("v1/devices/me/rpc/request/+", 0)
                        if (it.isActive)
                            it.resume(true)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.d(TAG, "Connection failure:${exception!!.message}")
                        it.resumeWithException(exception)
                    }
                })
            } catch (exception: MqttException) {
                exception.printStackTrace()
                it.resumeWithException(exception)
            }

        }


    private suspend fun publish(
        msg: MqttRpcCommand
    ) =
        suspendCancellableCoroutine<Boolean> {
            try {
                request++
                val message = MqttMessage()
                message.payload = gson.toJson(msg).toString().toByteArray()
                mqttClient.publish(topic, message, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.d(TAG, "$msg published to $topic")
                        it.resume(true)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable) {
                        Log.d(TAG, "Failed to publish $msg to $topic")
                        it.resumeWithException(exception)
                    }
                })
            } catch (exception: MqttException) {
                exception.printStackTrace()
                it.resumeWithException(exception)
            }
        }

    suspend fun attachDeviceToUser(username: String) {
        val payload = MqttRpcCommand(
            method = MqttRpcCommandMethod.AttachDeviceToUser,
            mapOf("username" to username)
        )
        publish(
            payload
        )
    }

    suspend fun declineTask(taskName: String) {
        val payload = MqttRpcCommand(MqttRpcCommandMethod.EndTask, mapOf("taskName" to taskName))
        publish(payload)
    }

    suspend fun updateCheckList(
        checkListName: String,
        checkListResultMap: MutableMap<String, Int>
    ) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.UpdateCheckList,
            mapOf("checkListName" to checkListName, "items" to checkListResultMap)
        )
        publish(payload)
    }

    suspend fun startTask(taskName: String) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.StartTask,
            mapOf("username" to username!!, "taskName" to taskName)
        )
        publish(payload)
    }

    suspend fun attachRfid(rfidTagEpc: String, vehicleName: String) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.AttachRfidTagToVehicle,
            mapOf("epc" to rfidTagEpc, "vehicleName" to vehicleName)
        )
        publish(payload)
    }

    suspend fun endTask(
        taskType: String,
        success: Boolean,
        epc: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.EndTask,
            mapOf(
                "success" to success,
                "TaskType" to taskType,
                "latitude" to latitude,
                "longitude" to longitude,
                "epc" to epc
            )
        )
        publish(payload)
    }

    suspend fun attachDefectToVehicle(defectName: String, vehicleName: String) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.AttachDefectToVehicle,
            mapOf("defectName" to defectName, "vehicleName" to vehicleName)
        )
        publish(payload)
    }

    suspend fun detachDefectFromVehicle(defectName: String, vehicleName: String) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.DetachDefectFromVehicle,
            mapOf("defectName" to defectName, "vehicleName" to vehicleName)
        )
        publish(payload)
    }

    suspend fun allowPassage(taskType: String, siteRow: String? = null) {
        val payload = MqttRpcCommand(
            MqttRpcCommandMethod.AllowPassage,
            mapOf("taskType" to taskType, "siteRow" to siteRow)
        )
        publish(payload)
    }
}

interface MqttMessageListener {
    fun messageArrived(result: AttachResult, topic: String?)
}
