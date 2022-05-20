package com.starostinvlad.tsdapp.checklist_screen

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.api.ThingsBoardApi
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.*
import kotlinx.coroutines.launch
import javax.inject.Inject


class CheckListFragmentPresenter @Inject constructor(
    private val gson: Gson,
    private val mqttHelper: MQTTHelper
) :
    BasePresenter<CheckListFragmentContract>(), MqttMessageListener {

    private lateinit var checkListName: String
    private val TAG = this.javaClass.simpleName

    private var checkList: MutableList<CheckBoxListItem> = mutableListOf()

    lateinit var item: Entity

    override fun attachView(mvpView: CheckListFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    fun onLoaded(item: Entity) {
        this.item = item
        loadData()
    }

    private fun parseCheckItems(items: String): Map<String, String> = gson.fromJson(
        items, object : TypeToken<HashMap<String, String>>() {}.type
    )

    private fun parseItems(items: String): Map<String, Int> = gson.fromJson(
        items, object : TypeToken<HashMap<String, Int>>() {}.type
    )

    private fun loadData() {
        view?.showLoading(true)
        checkList.clear()
        launch {
            try {
                checkListName = item.latest.entityField?.get("name")?.value!!
                val checkItems = item.latest.serverAttribute?.get("checkItems")!!.value
                val items = item.latest.serverAttribute?.get("items")!!.value
                val checkItemsMap = parseCheckItems(checkItems)
                val itemsMap = parseItems(items)
                Log.d("loadData", checkItemsMap.toString())
                checkList = checkItemsMap.entries.map {
                    CheckBoxListItem(
                        lastUpdateTs = 0,
                        key = it.key,
                        value = it.value as String,
                        status = (itemsMap[it.key] ?: -1) as Int
                    )
                }.toMutableList()
                Log.d(TAG, "loadData: list:$checkList")
                view?.showLoading(false)
                if (checkList.isNotEmpty())
                    view?.showResult(checkList)
                else
                    view?.showError("Чек-лист пуст")
            } catch (e: Exception) {
                e.printStackTrace()
                view?.showError(e.localizedMessage)
            }
        }
    }

    fun onSubmitBtnClick() {
        updateChecklist()
    }

    fun onUploadBtnClick() {
        if (checkList.any { it.status == -1 }) {
            view?.showNotCompleteDialog()
            return
        }
        updateChecklist()
    }

    private fun updateChecklist() = launch {
        checkList.forEach {
            if (it.status == -1)
                it.status = 0
        }
        sendUpdateData()
    }

    private suspend fun sendUpdateData() {
        view?.showLoading(true)
        val checkListResultMap: MutableMap<String, Int> =
            mutableMapOf(checkList.map {
                it.key to it.status
            }.first())

        checkList.forEach {
            checkListResultMap[it.key] = it.status
        }
        mqttHelper.updateCheckList(checkListName, checkListResultMap)
    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        Log.d(
            "checkListFragment",
            "Message: ${result.params.message} from topic: $topic"
        )

        view?.showLoading(false)

        if (result.params.success) {
            if (result.method == MqttRpcCommandMethod.UpdateCheckList) {
                view?.requestSucces()
            }
        } else if (!result.params.success) {
            view?.showError(result.params.message)
        }
    }
}