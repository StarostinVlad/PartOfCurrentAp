package com.starostinvlad.tsdapp.login_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView

interface LoginFragmentContract : BaseView {
    fun showLoginFailed(errorString: String)
    fun updateUiWithUser()
    fun enableSubmit(enable:Boolean)
    fun showLoading(show:Boolean)
    fun getImei():String
    fun openSettingScreen()
}