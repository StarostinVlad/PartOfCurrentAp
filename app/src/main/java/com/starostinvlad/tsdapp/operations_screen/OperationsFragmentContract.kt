package com.starostinvlad.tsdapp.operations_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityId

interface OperationsFragmentContract : BaseView {
    fun openWorkFlow(entityId: EntityId)
    fun showIncompleteTask(entity: Entity)
    fun hideIncompletedTask()
    fun showError(message: String)
    fun logout()
    fun openConfirmLocation(entityId: EntityId)
}