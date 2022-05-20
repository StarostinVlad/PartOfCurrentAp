package com.starostinvlad.tsdapp.confirm_location_screen

import android.util.Log
import com.google.gson.Gson
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.AttachResult
import com.starostinvlad.tsdapp.data.MqttRpcCommandMethod
import com.starostinvlad.tsdapp.data.ThingsboardApiHelper
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import com.starostinvlad.tsdapp.rfid.RfidListener
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class ConfirmLocationFragmentPresenter @Inject constructor(
    private val mqttHelper: MQTTHelper,
    private val api: ThingsboardApiHelper,
    private val rfidHelper: RFIDHelper,
    private val gson: Gson
) :
    BasePresenter<ConfirmLocationFragmentContract>(), MqttMessageListener, RfidListener {


    private var _rfidTagEpc: String? = null
    private val TAG = this.javaClass.simpleName

    override fun attachView(mvpView: ConfirmLocationFragmentContract) {
        mqttHelper.addListener(this)
        rfidHelper.addListener(this)
        super.attachView(mvpView)
    }

    fun onLoaded() = launch {
        Log.d(TAG, "attachView: ${api.row?.latest?.serverAttribute}")
        if (api.row == null)
            api.row =
                api.userId?.let { userId ->
                    api.findIncompleteTask(userId)?.data?.firstOrNull {
                        it.latest.entityField?.get(
                            "type"
                        )?.value == "TaskAcceptance"
                    }?.let { task ->
                        api.findTaskChildren(task.entityId)?.data?.firstOrNull {
                            it.latest.entityField?.get(
                                "type"
                            )?.value == "Vehicle"
                        }?.let { vehicle ->
                            api.findVehicleSiteRow(vehicle.entityId)
                        }
                    }
                }
        api.row?.latest?.serverAttribute?.let {
            val perimetrStr = it["perimeter"]!!.value
            val perimetrStrArray = perimetrStr.substring(2, perimetrStr.length - 2).split("],[")
            val points: MutableList<GeoPoint> = emptyList<GeoPoint>().toMutableList()
            perimetrStrArray.forEach { str ->
                Log.d(TAG, "attachView: str:$str")
                val coord = str.split(",")
                Log.d(TAG, "attachView: coord:$coord")
                val geoPoint = GeoPoint(coord[0].trim().toDouble(), coord[1].trim().toDouble())
                Log.d(TAG, "attachView: geoPoint:$geoPoint")
                points.add(geoPoint)
            }
            points.add(points[0])
            view?.showRow(points)
            val geoPoint =
                GeoPoint(it["latitude"]!!.value.toDouble(), it["longitude"]!!.value.toDouble())
            view?.initMap(geoPoint)
        }
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        rfidHelper.removeListener(this)
        super.detachView()
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        Log.d(
            "workFlowFragment",
            "Message: ${result.params.message} from topic: $topic"
        )
        view?.showLoading(false)

        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.EndTask) view?.closeTask()
        } else if (!result.params.success) {
            view?.showError(result.params.message)
        }

    }

    fun onConfirmLocationBtnClick() {
        rfidHelper.readRfidTag()
    }

    fun onLocationRecv(lat: Double, long: Double) = launch {
        val vehicleName =
            api.taskChildren!!.data.first { entity -> entity.latest.entityField?.get("type")?.value == "Vehicle" }.latest.entityField?.get(
                "name"
            )?.value
//        if(api.row.l)
        vehicleName?.let {
            mqttHelper.endTask(
                "TaskAcceptance",
                latitude = lat,
                longitude = long,
                success = true,
                epc = _rfidTagEpc
            )
        }
    }

    override fun onTagRead(rfidTagEpc: String) {
        launch {
            view?.showLoading(true)
            _rfidTagEpc = rfidTagEpc

            if (api.vehicleChildren == null || api.vehicleChildren?.data.isNullOrEmpty()) {
                val vehicleId =
                    api.taskChildren?.data?.firstOrNull { entity -> entity.latest.entityField?.get("type")?.value == "Vehicle" }?.entityId
                Log.d("onLoaded", "entityId: $vehicleId")
                vehicleId?.let {
                    api.findVehicleChildren(entityId = it)
                }
            }
            Log.d(TAG, "vehicle children: ${api.vehicleChildren?.data}")
            if (api.vehicleChildren?.data.isNullOrEmpty()) {
                view?.showLoading(false)
                view?.showError("Неизвестная ошибка")
                return@launch
            }
            api.vehicleChildren?.data?.firstOrNull { entity -> entity.latest.entityField?.get("type")?.value == "RfidTag" }?.latest?.serverAttribute?.get(
                "Epc"
            )?.let {
                Log.d(TAG, "onTagRead: $rfidTagEpc : ${it.value}")
                if (it.value == rfidTagEpc) {
                    view?.getLastLocation()
                } else {
                    view?.showLoading(false)
                    view?.showError("Отсканированая метка не соответствует!")
                }
            }
        }

    }
}