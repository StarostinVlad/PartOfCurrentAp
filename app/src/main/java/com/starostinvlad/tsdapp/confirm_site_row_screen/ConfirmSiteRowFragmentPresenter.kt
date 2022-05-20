package com.starostinvlad.tsdapp.confirm_site_row_screen

import android.util.Log
import com.starostinvlad.tsdapp.api.MQTTHelper
import com.starostinvlad.tsdapp.api.MqttMessageListener
import com.starostinvlad.tsdapp.base_mvp.BasePresenter
import com.starostinvlad.tsdapp.data.AttachResult
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.MqttRpcCommandMethod
import com.starostinvlad.tsdapp.data.ThingsboardApiHelper
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import com.starostinvlad.tsdapp.rfid.RfidListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfirmSiteRowFragmentPresenter @Inject constructor(
    private val mqttHelper: MQTTHelper,
    private val api: ThingsboardApiHelper
) :
    BasePresenter<ConfirmSiteRowFragmentContract>(), MqttMessageListener {

    override fun attachView(mvpView: ConfirmSiteRowFragmentContract) {
        mqttHelper.addListener(this)
        super.attachView(mvpView)
    }

    override fun detachView() {
        mqttHelper.removeListener(this)
        super.detachView()
    }

    private var rows: List<Entity>? = null
    private var sites: List<Entity>? = null
    private val TAG = this.javaClass.simpleName

    fun onLoaded() = launch {
        sites = if (api.additionalInfo == null)
            api.getSites().data
        else
            api.getLocationSites(api.additionalInfo!!.locationId).data
        sites.let {
            if (it!!.isNotEmpty()) {
                view?.showSites(it.map { entity -> entity.latest.entityField?.get("name")!!.value })
            }
        }
    }

    fun onSiteConfirmed(itemPosition: Int) = launch {
        Log.d(TAG, "onSiteConfirmed: position:$itemPosition")
        sites?.get(itemPosition)?.entityId?.let {
            rows = api.getSiteRows(it).data
            val rowsString =
                rows!!.filter { entity -> entity.latest.entityField?.get("name") != null }
                    .map { entity -> entity.latest.entityField?.get("name")!!.value }
                    .toList()
            view?.showSitesRows(rowsString)
        }
    }

    fun onRowConfirmed(sitePosition: Int, rowPosition: Int) = launch {
        api.site = sites?.get(sitePosition)
        api.row = rows?.get(rowPosition)
        api.row?.let {
            view?.showLoading(true)
            mqttHelper.allowPassage("TaskAcceptance", it.latest.entityField?.get("name")?.value)
        }

    }

    override fun messageArrived(result: AttachResult, topic: String?) {
        view?.showLoading(false)
        if (result.method == MqttRpcCommandMethod.AllowPassage)
            if (result.params.success) {
                view?.showLocationConfirm()
            } else {
                view?.showError(result.params.message)
            }
    }
}