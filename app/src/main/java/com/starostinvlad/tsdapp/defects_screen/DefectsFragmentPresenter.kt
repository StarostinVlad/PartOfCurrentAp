package com.starostinvlad.tsdapp.defects_screen

import android.util.Log
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefectsFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<DefectsFragmentContract>(), MqttMessageListener {

    var _defect: DefectData? = null

    override fun attachView(mvpView: DefectsFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onLoaded() = launch {
        if (api.vehicle == null) {
            val vehicleId =
                api.taskChildren!!.data.first { entity -> entity.latest.entityField?.get("type")?.value == "Vehicle" }.entityId
            Log.d("onLoaded", "entityId: $vehicleId")
            vehicleId.let {
                api.findVehicleChildren(entityId = vehicleId)
            }
        } else
            api.vehicle?.data?.let {
                if (it.isNotEmpty()) {
                    api.findVehicleChildren(it.first().entityId)
                }
            }
        Log.d(
            "onLoaded", "data:${
                api.vehicleChildren!!.data
            }"
        )
        api.vehicleChildren!!.data.filter { entity -> entity.latest.entityField?.get("type")?.value == "Defect" }
            .let {
                api.addDefects(it.map { entity ->
                    DefectData(
                        entity.latest.entityField?.get("name")!!.value,
                        "",
                        true
                    )
                })
            }
        view?.showDefects(api.defects)
    }

    fun onAddDefectBtnClick() {

    }

    fun onRemoveDefectBtnClick(defect: DefectData) = launch {
        _defect = defect
        val vehicleName =
            api.taskChildren!!.data.first { entity -> entity.latest.entityField?.get("type")?.value == "Vehicle" }.latest.entityField?.get(
                "name"
            )?.value

        val defectName = defect.title

        Log.d("onDefectsItemSelected", "$vehicleName : $defectName")
        if (vehicleName != null) {
            mqttHelper.detachDefectFromVehicle(
                defectName = defectName,
                vehicleName = vehicleName
            )
        }
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.DetachDefectFromVehicle) {
                api.removeDefect(_defect!!)
                view?.showDefects(api.defects)
            }
        }
    }
}