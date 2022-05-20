package com.starostinvlad.tsdapp.search_defect_dialog

import com.starostinvlad.tsdapp.base_mvp.BaseView
import com.starostinvlad.tsdapp.data.DefectData
import com.starostinvlad.tsdapp.data.Entity

interface SearchFragmentContract : BaseView {
    fun showDefects(defects: List<DefectData>)
    fun showEmptyList()
    fun changeDefect(defectData: DefectData)
}