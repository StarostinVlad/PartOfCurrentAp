package com.starostinvlad.tsdapp.confirm_location_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import org.osmdroid.util.GeoPoint

interface ConfirmLocationFragmentContract : BaseView {
    fun closeTask()
    fun showError(message: String)
    fun showLoading(show: Boolean)
    fun getLastLocation()
    fun initMap(geoPoint: GeoPoint)
    fun showRow(points: MutableList<GeoPoint>)
}