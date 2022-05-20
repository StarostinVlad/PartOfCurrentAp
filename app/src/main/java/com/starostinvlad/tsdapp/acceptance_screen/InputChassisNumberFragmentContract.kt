package com.starostinvlad.tsdapp.acceptance_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityData
import com.starostinvlad.tsdapp.data.EntityId

interface InputChassisNumberFragmentContract : BaseView {
    fun showTaskList(taskListId: EntityId, fromRfid: Boolean)
    fun showError(message: String)
    fun showRfidError(message: String)
    fun showLoading(show: Boolean)
}