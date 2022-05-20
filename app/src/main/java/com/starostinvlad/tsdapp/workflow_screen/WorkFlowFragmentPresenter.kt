package com.starostinvlad.tsdapp.workflow_screen

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkFlowFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val gson: Gson,
    private val mqttHelper: MQTTHelper
) : BasePresenter<WorkFlowFragmentContract>(), MqttMessageListener {
    val TAG: String = this.javaClass.simpleName

    override fun attachView(mvpView: WorkFlowFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onLoaded(entityId: EntityId) {
        view?.showLoading(true)
        launch {
            try {
                api.findTaskChildren(entityId)
                view?.showLoading(false)
                api.taskChildren.let { taskChildren ->
                    taskChildren!!.data.filter { entity -> entity.latest.entityField?.get("type")?.value == "CheckList" }
                        .let { view?.showResult(it) }
//                    taskChildren.data.first().latest.entityField!!["name"]?.let { view?.setTitle(it.value) }
                    api.task!!.data.first { entity -> entity.latest.entityField?.get("type")!!.value == "TaskAcceptance" }.latest.entityField?.get(
                        "name"
                    )?.value?.let {
                        view?.setTitle(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                view?.showLoading(false)
                e.message?.let { view?.showError(it) }
            }
        }
    }

    fun onSelectedItem(item: Entity) {
        view?.openCheckList(item)
    }

    private fun parseItems(items: String): Map<String, Int> = gson.fromJson(
        items, object : TypeToken<HashMap<String, Int>>() {}.type
    )

    fun onEndTaskClick() = launch {
        var notCondition = false
        var withDefect = false
        api.task?.data?.first()?.entityId?.let { api.findTaskChildren(it) }
        api.taskChildren?.let { taskChild ->
            taskChild.data.filter { entity -> entity.latest.entityField?.get("type")?.value == "CheckList" }
                .let {
                    it.forEach { checklist ->
                        val items = checklist.latest.serverAttribute?.get("items")!!.value
                        val itemsMap = parseItems(items)
                        itemsMap.entries.forEach { entry ->
                            if (entry.value < 1)
                                notCondition = true
                        }
                    }
                }
        }
        api.vehicleChildren?.let { vehicleChild ->
            withDefect =
                vehicleChild.data.any { entity -> entity.latest.entityField?.get("type")?.value == "Defect" }
        }
        if (notCondition) {
            view?.showAcceptance("Не кондиция!Принять?")
        } else if (withDefect) {
            view?.showAcceptance("На технике имеются дефекты!Принять?")
        } else {
            view?.showAcceptance("Принять технику?")
        }

    }

    fun onSubmitBtnClick() {
        view?.openConfirmLocationScreen()
    }

    fun onDeclineBtnClick() = launch {
        view?.showLoading(true)
        api.task!!.data.first { entity -> entity.latest.entityField?.get("type")!!.value == "TaskAcceptance" }.latest.entityField?.get(
            "name"
        )?.value?.let {
            mqttHelper.endTask("TaskAcceptance", false)
        }
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        view?.showLoading(false)
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.EndTask)
                view?.returnToHomeScreen()
        } else if (!result.params.success) {
            view?.showError(result.params.message)
        } else
            Log.d(
                "WorkflowPresenter",
                "Receive message: ${result.params.message} from topic: $topic"
            )
    }
}