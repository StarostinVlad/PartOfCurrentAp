package com.starostinvlad.tsdapp.checklist_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.CheckBoxListItem

interface CheckListFragmentContract : BaseView {
    fun showResult(result: List<CheckBoxListItem>)
    fun requestSucces()
    fun showError(message: String?)
    fun showLoading(show: Boolean)
    fun showNotCompleteDialog()
}