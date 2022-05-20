package com.starostinvlad.tsdapp.defects_screen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.DefectsFingerprint
import com.starostinvlad.tsdapp.adapter.fingerprints.WorkFlowFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.DefectData
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.databinding.FragmentDefectsBinding
import javax.inject.Inject

class DefectsFragment : Fragment(R.layout.fragment_defects), DefectsFragmentContract {

    private val binding: FragmentDefectsBinding by viewBinding()
    private lateinit var adapter: FingerprintAdapter

    @Inject
    lateinit var presenter: DefectsFragmentPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        presenter.onLoaded()
        adapter = FingerprintAdapter(DefectsFingerprint {
            presenter.onRemoveDefectBtnClick(it)
        })
        binding.defectsRV.adapter = adapter
        binding.addDefectsBtn.setOnClickListener {
//            presenter.onAddDefectBtnClick()
            findNavController().navigate(R.id.searchFragment)
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun showDefects(defects: List<DefectData>) {
        adapter.setItems(defects)
    }
}