package com.starostinvlad.tsdapp.login_screen

import android.media.MediaDrm
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.databinding.FragmentLoginBinding
import com.starostinvlad.tsdapp.main_screen.InterfaceName
import com.starostinvlad.tsdapp.main_screen.MainActivity
import java.util.*
import javax.inject.Inject


class LoginFragment : Fragment(R.layout.fragment_login), LoginFragmentContract, InterfaceName {

    private lateinit var mainActivity: MainActivity
    private val binding: FragmentLoginBinding by viewBinding()

    @Inject
    lateinit var presenter: LoginFragmentPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.appComponent?.inject(this)
        activity?.setActionBar(binding.toolbarLoginScreen)
        presenter.attachView(this)

        with(binding) {
            loginBtn.isEnabled = false

            val afterTextChangedListener = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // ignore
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    // ignore
                }

                override fun afterTextChanged(s: Editable) {
                    presenter.loginDataChanged(
                        usernameInput.text.toString(),
                        passwordInput.text.toString()
                    )
                }
            }
            usernameInput.addTextChangedListener(afterTextChangedListener)
            passwordInput.addTextChangedListener(afterTextChangedListener)


            passwordInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login()
                }
                false
            }
            loginBtn.setOnClickListener {
                login()
            }
        }
    }

    private fun login() {
        with(binding) {
            presenter.onLoginSubmit(
                usernameInput.text.toString(), passwordInput.text.toString()
            )
        }
    }

    override fun showLoading(show: Boolean) {
        with(binding) {
            loading.isVisible = show
            usernameInput.isEnabled = !show
            passwordInput.isEnabled = !show
            loginBtn.isEnabled = !show
        }
    }

    override fun onStart() {
        super.onStart()
        mainActivity = activity as MainActivity
        mainActivity.attachReaderListener(this)
    }

    override fun onDestroyView() {
        presenter.detachView()
        mainActivity.detachReaderListener()
        super.onDestroyView()
    }

    override fun updateUiWithUser() {
        findNavController().navigate(R.id.operationsFragment)
    }

    override fun showLoginFailed(errorString: String) {
        with(binding)
        {
            passwordInputStyle.error = errorString
            usernameInputStyle.error = errorString
        }
    }

    override fun enableSubmit(enable: Boolean) {
        binding.loginBtn.isEnabled = enable
    }

    override fun getImei(): String {
        return getDeviceUniqueID()
    }

    override fun openSettingScreen() {
        findNavController().navigate(R.id.settingsFragment)
    }

    private fun getDeviceUniqueID(): String {
        val wideVineUuid = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
        return try {
            val wvDrm = MediaDrm(wideVineUuid)
            val wideVineId = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
            val stringWithSymbols: String = wideVineId.contentToString()
            val strWithoutBrackets = stringWithSymbols.replace("\\[".toRegex(), "")
            val strWithoutBrackets1 = strWithoutBrackets.replace("]".toRegex(), "")
            val strWithoutComma = strWithoutBrackets1.replace(",".toRegex(), "")
            val strWithoutHyphen = strWithoutComma.replace("-".toRegex(), "")
            val strWithoutSpace = strWithoutHyphen.replace(" ".toRegex(), "")
            strWithoutSpace.substring(0, 15)
        } catch (e: Exception) {
            ""
        }
    }

    override fun onTagReaded(tagId: ByteArray) {
        val tagIdString = tagId.slice(0..4).reversed().toByteArray().toHexString()
        with(binding)
        {
            usernameInput.setText(getString(R.string.emailTemplate, tagIdString))
            passwordInput.setText(tagIdString)
        }
        login()
    }

    override fun onBarcodeReaded(barcode: String) {
        with(binding) {
            usernameInput.setText(getString(R.string.emailTemplate, barcode))
            passwordInput.setText(barcode)
        }
        login()
    }

    private fun ByteArray.toHexString(): String {
        return this.joinToString("") {
            java.lang.String.format("%02x", it).uppercase()
        }
    }
}