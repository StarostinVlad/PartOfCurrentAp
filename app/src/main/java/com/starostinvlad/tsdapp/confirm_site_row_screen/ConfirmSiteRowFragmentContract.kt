package com.starostinvlad.tsdapp.confirm_site_row_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView

interface ConfirmSiteRowFragmentContract : BaseView {
    fun showError(message: String)
    fun showSites(sites: List<String>)
    fun showLocationConfirm()
    fun showSitesRows(rows: List<String>)
    fun showLoading(show: Boolean)
}