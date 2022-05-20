package com.starostinvlad.tsdapp.confirm_site_row_screen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.databinding.FragmentConfirmSiteRowBinding
import javax.inject.Inject

class ConfirmSiteRowFragment : Fragment(R.layout.fragment_confirm_site_row),
    ConfirmSiteRowFragmentContract {

    @Inject
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val binding: FragmentConfirmSiteRowBinding by viewBinding()

    @Inject
    lateinit var presenter: ConfirmSiteRowFragmentPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        presenter.onLoaded()

        with(binding) {
            btnConfirmSite.setOnClickListener {
                presenter.onRowConfirmed(
                    siteList.selectedItemPosition,
                    rowList.selectedItemPosition
                )
            }

            siteList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    Log.d("TAG", "onItemSelected: $position")
                    presenter.onSiteConfirmed(position)
                }

            }
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun showSites(sites: List<String>) {
        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, sites)
        binding.siteList.adapter = adapter
//        binding.siteList.setSelection(0)
    }

    override fun showLocationConfirm() {
        findNavController().navigate(R.id.confirmLocationFragment, null, navOptions {
            popUpTo(R.id.confirmLocationFragment) {
                inclusive = true
            }
        })
    }

    override fun showSitesRows(rows: List<String>) {
        binding.containerSetRow.isVisible = true
        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, rows)
        binding.rowList.adapter = adapter
    }

    override fun showLoading(show: Boolean) {
        binding.containerSetRow.isVisible = !show
        binding.containerSetSite.isVisible = !show
        binding.progress.isVisible = show
    }

    override fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

}