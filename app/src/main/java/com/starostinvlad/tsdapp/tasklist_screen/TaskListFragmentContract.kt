package com.starostinvlad.tsdapp.tasklist_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityId

interface TaskListFragmentContract : BaseView {
    fun openTask(entityId: EntityId)
    fun showTaskList(taskList: List<Entity>)
    fun showError(message: String)
    fun showLoading(show: Boolean)
    fun openAcceptanceTask(entityId: EntityId)
}