package com.starostinvlad.tsdapp.search_defect_dialog

import android.util.Log
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<SearchFragmentContract>(), MqttMessageListener {

    private var _defect: DefectData? = null

    override fun attachView(mvpView: SearchFragmentContract) {
        mqttHelper.addListener(this)
        searchQuery("")
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onTextChanged(query: String) = launch {
        delay(500)
        searchQuery(query)
    }

    private fun searchQuery(query: String) = launch {
        val defects = api.searchDefects(query).data
        if (defects.isNotEmpty()) {
            view?.showDefects(defects.map { entity ->
                val title = entity.latest.entityField?.get("name")!!.value
                DefectData(
                    title,
                    "",
                    api.defects.any { defectData -> defectData.title == title }
                )
            })
        } else {
            view?.showEmptyList()
        }
    }

    fun onDefectsItemSelected(defect: DefectData) = launch {
        Log.d("onDefectsItemSelected", "entity:${defect}")

        defect.status = true
        api.addDefect(defect)
        _defect = defect

        val vehicleName =
            api.taskChildren!!.data.first { entity -> entity.latest.entityField?.get("type")?.value == "Vehicle" }.latest.entityField?.get(
                "name"
            )?.value

        val defectName = defect.title

        Log.d("onDefectsItemSelected", "$vehicleName : $defectName")
        if (vehicleName != null) {
            mqttHelper.attachDefectToVehicle(
                defectName = defectName,
                vehicleName = vehicleName
            )

        }
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.AttachDefectToVehicle) {
                view?.changeDefect(_defect!!)
            }
        }
    }
}