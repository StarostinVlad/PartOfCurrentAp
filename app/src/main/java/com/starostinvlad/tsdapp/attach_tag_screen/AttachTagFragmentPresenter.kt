package com.starostinvlad.tsdapp.attach_tag_screen

import android.util.Log
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import com.starostinvlad.tsdapp.rfid.RfidListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttachTagFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val rfidHelper: RFIDHelper,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<AttachTagFragmentContract>(), RfidListener, MqttMessageListener {

    private var epcTag: String? = null
    private var _entityId: EntityId? = null

    override fun attachView(mvpView: AttachTagFragmentContract) {
        rfidHelper.addListener(this)
        mqttHelper.addListener(this)
        Log.d("attachView", "addListener")
        super.attachView(mvpView)
    }

    override fun detachView() {
        rfidHelper.removeListener(this)
        mqttHelper.removeListener(this)
        Log.d("detachView", "removeListener")
        super.detachView()
    }


    fun onLoaded(entityId: EntityId) = launch {
        _entityId = entityId
        val taskChildren =
            api.findTaskChildren(entityId)?.data
        if (taskChildren!!.isNotEmpty()) {
            val rfidTag = taskChildren.firstOrNull {
                it.latest.entityField?.get("type")?.value == "RfidTag"
            }
            rfidTag?.latest?.entityField?.get("name")?.let {
                if (it.value.isNotEmpty()) {
                    view?.showMessage("Отсканируйте метку")
                    epcTag = it.value
                }
            }
            if (rfidTag == null)
                view?.showMessage("Отсканируйте метку чтобы прикрепить к авто")
        } else {
            view?.showMessage("Что-то не так")
        }
    }

    override fun onTagRead(rfidTagEpc: String) {
        launch {
            Log.d("onTagRead", "epc:$rfidTagEpc")
            view?.showLoading(true)

            val rfidTag = api.findRfidTagByEPC(rfidTagEpc)!!
            if (rfidTag.data.isEmpty()) {
                api.vehicle?.data?.firstOrNull()?.latest?.entityField?.get("name")?.let {
                    mqttHelper.attachRfid(
                        rfidTagEpc = rfidTagEpc,
                        it.value
                    )
                }
            } else {
                view?.showLoading(false)
                rfidTag.data.first().latest.serverAttribute.let { serverAttributes ->
                    if (serverAttributes!!.containsKey("Epc") && serverAttributes["Epc"]!!.value == rfidTagEpc) {
                        view?.openWorkflow(_entityId!!)
                    } else
                        view?.showRfidError("Отсканированная метка не соответствует заданию")
                }

            }
        }
    }

    fun readTag() {
        rfidHelper.readRfidTag()
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        view?.showLoading(false)
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.AttachRfidTagToVehicle) {
                view?.openWorkflow(entityId = _entityId!!)
            }
        } else {
            view?.showRfidError(result.params.message)
        }
    }
}