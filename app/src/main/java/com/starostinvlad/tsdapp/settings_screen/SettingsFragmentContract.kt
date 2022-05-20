package com.starostinvlad.tsdapp.settings_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView

interface SettingsFragmentContract:BaseView {
    fun showCurrentSettings(scheme: String, host: String, port: Int, token: String)
    fun hostSaved()
    fun showHostError(msg:String)
    fun showPortError(msg:String)
    fun showTokenError(msg:String)
    fun showLoading(loading: Boolean)
    fun hideErrors()
}