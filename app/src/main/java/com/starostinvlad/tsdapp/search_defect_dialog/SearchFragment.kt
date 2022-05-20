package com.starostinvlad.tsdapp.search_defect_dialog

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.SearchDefectsFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.DefectData
import com.starostinvlad.tsdapp.databinding.FragmentSearchDefectsBinding
import javax.inject.Inject


class SearchFragment : Fragment(R.layout.fragment_search_defects), SearchFragmentContract {
    private lateinit var adapter: FingerprintAdapter
    private val binding: FragmentSearchDefectsBinding by viewBinding()

    @Inject
    lateinit var presenter: SearchFragmentPresenter


    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        binding.btnClearSearch.setOnClickListener {
            binding.searchView.setText("")
        }
        adapter = FingerprintAdapter(SearchDefectsFingerprint {
            presenter.onDefectsItemSelected(it)
        })
        binding.searchedDefectsRV.adapter = adapter

        binding.searchView.addTextChangedListener { text ->
            presenter.onTextChanged(text.toString())
        }
    }

    override fun showDefects(defects: List<DefectData>) {
        binding.searchedDefectsRV.isVisible = true
        binding.textSearchMessage.isVisible = false
        adapter.setItems(defects)
    }

    override fun showEmptyList() {
        binding.searchedDefectsRV.isVisible = false
        binding.textSearchMessage.isVisible = true
    }

    override fun changeDefect(defectData: DefectData) {
        adapter.getItemPosition(defectData)?.let { adapter.notifyItemChanged(it) }
    }
}