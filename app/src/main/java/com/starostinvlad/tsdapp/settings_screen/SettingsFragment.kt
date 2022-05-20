package com.starostinvlad.tsdapp.settings_screen

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.databinding.FragmentSettingsBinding
import javax.inject.Inject


class SettingsFragment : Fragment(R.layout.fragment_settings), SettingsFragmentContract {
    val binding: FragmentSettingsBinding by viewBinding()

    @Inject
    lateinit var presenter: SettingsFragmentPresenter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        activity?.actionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.actionBar?.setHomeButtonEnabled(true)

        presenter.attachView(this)

        with(binding)
        {
            domainInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveSettings()
                }
                false
            }
            btnSaveDomain.setOnClickListener {
                saveSettings()
            }
        }
    }

    private fun saveSettings() {
        with(binding)
        {
            presenter.onSaveSettings(
                schemaSpinner.selectedItem.toString(),
                domainInput.text.toString(),
                portInput.text.toString(),
                tokenInput.text.toString()
            )
        }
    }

    override fun showCurrentSettings(scheme: String, host: String, port: Int, token: String) {
        with(binding)
        {
            binding.schemaSpinner.setSelection(
                (schemaSpinner.adapter as ArrayAdapter<String>
                        ).getPosition(
                        scheme
                    )
            )
            domainInput.setText(host)
            portInput.setText(port.toString())
            tokenInput.setText(token)
        }
    }

    override fun hostSaved() {
        findNavController().navigate(R.id.loginFragment, null, navOptions {
            popUpTo(R.id.settingsFragment) {
                inclusive = true
            }
        })
    }

    override fun showHostError(msg: String) {
        binding.domainInputStyle.error = msg
    }

    override fun showTokenError(msg: String) {
        binding.tokenInputStyle.error = msg
    }

    override fun showPortError(msg: String) {
        binding.portInputStyle.error = msg
    }

    override fun showLoading(loading: Boolean) {
        with(binding)
        {
            domainInput.isEnabled = !loading
            portInput.isEnabled = !loading
            tokenInput.isEnabled = !loading
            btnSaveDomain.isEnabled = !loading
            schemaSpinner.isEnabled = !loading
            loadingSettings.isVisible = loading
        }
    }

    override fun hideErrors() {
        with(binding) {
            domainInputStyle.error = null
            portInputStyle.error = null
            tokenInputStyle.error = null
        }
    }
}