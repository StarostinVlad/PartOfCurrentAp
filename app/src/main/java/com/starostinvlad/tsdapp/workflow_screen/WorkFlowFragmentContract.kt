package com.starostinvlad.tsdapp.workflow_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.FromTo
import com.starostinvlad.tsdapp.data.Name

interface WorkFlowFragmentContract : BaseView {
    fun showResult(result: List<Entity>)
    fun setTitle(title: String)
    fun openCheckList(item: Entity)
    fun showError(message: String)
    fun showLoading(show: Boolean)
    fun showToast(mqttHost: String)
    fun openConfirmLocationScreen()
    fun showAcceptance(message: String)
    fun returnToHomeScreen()
}