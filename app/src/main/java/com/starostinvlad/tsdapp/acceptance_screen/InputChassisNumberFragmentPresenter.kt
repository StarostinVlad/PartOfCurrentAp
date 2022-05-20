package com.starostinvlad.tsdapp.acceptance_screen

import android.util.Log
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import com.starostinvlad.tsdapp.rfid.RfidListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class InputChassisNumberFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val rfidHelper: RFIDHelper
) :
    BasePresenter<InputChassisNumberFragmentContract>(), RfidListener {


    override fun attachView(mvpView: InputChassisNumberFragmentContract) {
        rfidHelper.addListener(this)
        Log.d("attachView", "addListener")
        super.attachView(mvpView)
    }

    override fun detachView() {
        rfidHelper.removeListener(this)
        Log.d("detachView", "removeListener")
        super.detachView()
    }

    fun onFindBtnClick(text: String) = launch {
        if (isValid(text)) {
            view?.showLoading(true)
            val vehicles = api.findVehicleByChassisNumber(text)!!
            if (vehicles.data.isEmpty())
                view?.showError("Автотехника не найдена")
            else {
                val vehicle = vehicles.data.first()
                loadTask(vehicle.entityId, false)
            }
            view?.showLoading(false)
        }
    }


    private fun loadTask(entityId: EntityId, fromRfid: Boolean) = launch {
        val task = api.findTaskByVehicle(entityId)!!
        Log.d("onFindBtnClick", "task: ${task.data.size}")
        view?.showTaskList(entityId, fromRfid)
    }

    fun isValid(text: String): Boolean {
        return text.isNotEmpty() && text.length > 4
    }

    override fun onTagRead(rfidTagEpc: String) {
        launch {
            Log.d("onTagRead", "epc:$rfidTagEpc")

            val rfidTag = api.findRfidTagByEPC(rfidTagEpc)!!
            if (rfidTag.data.isEmpty()) {
                view?.showRfidError("Метка отсутствует в системе")
            } else {
                val vehicle = api.findVehicleByRfidTag(rfidTag.data.first().entityId)!!
                if (vehicle.data.isEmpty()) {
                    view?.showRfidError("Автотехника не найдена")
                } else
                    loadTask(vehicle.data.first().entityId, true)
            }
        }
    }

    fun readTag() {
        rfidHelper.readRfidTag()
    }
}