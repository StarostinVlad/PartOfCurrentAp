package com.starostinvlad.tsdapp.main_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView

interface MainActivityContract : BaseView {
    fun openWorkScreen()
    fun openLoginScreen()
    fun openSettingScreen()
    fun showError(error: String?)
}