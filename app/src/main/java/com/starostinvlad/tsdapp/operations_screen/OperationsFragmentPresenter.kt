package com.starostinvlad.tsdapp.operations_screen

import android.util.Log
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.api.ThingsBoardApi
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OperationsFragmentPresenter @Inject constructor(
    private val api: ThingsboardApiHelper,
    private val mqttHelper: MQTTHelper,
    private val preferences: Preferences
) :
    BasePresenter<OperationsFragmentContract>(), MqttMessageListener {

    override fun attachView(mvpView: OperationsFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    private var needShowMap: Boolean = false
    val TAG = this.javaClass.simpleName

    fun onLoaded() {
        launch {
            val result = api.userId?.let {
                Log.d(TAG, "onLoaded: userID:${it.entityType}")
                api.findIncompleteTask(it)
            }
            Log.d(TAG, "onLoaded: $result")
            if (result != null && result.data.isNotEmpty()) {
                if (result.data.any { it.latest.entityField?.get("type")?.value == "SiteRow" })
                    needShowMap = true
                val task =
                    result.data.firstOrNull { it.latest.entityField?.get("type")?.value == "TaskAcceptance" }
                task?.let {
                    api.findTaskChildren(it.entityId)
                    view?.showIncompleteTask(it)
                }
            }
        }
    }

    fun onAcceptedTask(entityId: EntityId) {
        if (needShowMap)
            view?.openConfirmLocation(entityId)
        else
            view?.openWorkFlow(entityId)
    }

    fun onDeclinedTask(taskName: String) = launch {
        mqttHelper.declineTask(taskName)
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        Log.d(
            TAG,
            "Receive message: ${result.params.message} from topic: $topic"
        )
        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.EndTask)

                view?.hideIncompletedTask()
        } else if (!result.params.success) {
            view?.showError(result.params.message)
        }
    }


    fun onLogoutBtnCLick() = launch {
        api.logout()
        view?.logout()
    }
}