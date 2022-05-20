package com.starostinvlad.tsdapp.defects_screen

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.DefectData
import com.starostinvlad.tsdapp.data.Entity

interface DefectsFragmentContract : BaseView {
    fun showDefects(defects: List<DefectData>)
}