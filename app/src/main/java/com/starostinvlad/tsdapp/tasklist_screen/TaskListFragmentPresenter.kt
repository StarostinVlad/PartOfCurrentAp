package com.starostinvlad.tsdapp.tasklist_screen

import android.util.Log
import com.google.gson.Gson
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import com.starostinvlad.tsdapp.rfid.RfidListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskListFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<TaskListFragmentContract>(), MqttMessageListener {
    //    private var vehicleChildren: Entity? = null
    private var epcTag: String? = null
    private var startEntity: Entity? = null
    private var fromRfid: Boolean = false

    override fun attachView(mvpView: TaskListFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onItemClick(entity: Entity) = launch {
        view?.showLoading(true)
        entity.latest.entityField?.get("name")?.let { mqttHelper.startTask(it.value) }
        startEntity = entity
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        Log.d(
            "inputChassisNumber", "Receive message: ${result.toString()} from topic: $topic"
        )
        view?.showLoading(false)

        if (result.params.success) {
            Log.d("onTagRead", "method: ${result.method}")
            if (result.method == MqttRpcCommandMethod.StartTask)
                startEntity?.let {
                    if (it.latest.entityField!!["type"]!!.value == "TaskAcceptance")
                        if (fromRfid)
                            view?.openTask(entityId = it.entityId)
                        else
                            view?.openAcceptanceTask(entityId = it.entityId)
                    else
                        view?.openTask(entityId = it.entityId)
                }
        } else if (!result.params.success) {
            view?.showError(result.params.message)
        } else
            Log.d(
                "taskListFragment",
                "Receive message: ${result.params.message} from topic: $topic"
            )
    }

    fun onLoaded(taskListId: EntityId, fromRfid: Boolean) {
        this.fromRfid = fromRfid
        launch {
            api.findTaskByVehicle(taskListId)!!
            showResult(api.task!!)
        }
    }

    private fun showResult(entityData: EntityData) {
        view?.showTaskList(entityData.data.filter { entity ->
            entity.latest.entityField?.get("type")?.value.equals(
                "TaskAcceptance"
            )
        })
    }
}