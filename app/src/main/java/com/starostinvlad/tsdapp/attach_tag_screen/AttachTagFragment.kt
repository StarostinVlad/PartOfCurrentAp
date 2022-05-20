package com.starostinvlad.tsdapp.attach_tag_screen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.databinding.FragmentAttachTagBinding
import javax.inject.Inject

class AttachTagFragment : Fragment(R.layout.fragment_attach_tag),
    AttachTagFragmentContract {
    val binding: FragmentAttachTagBinding by viewBinding()

    @Inject
    lateinit var presenter: AttachTagFragmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.appComponent?.inject(this)
    }

    override fun onStop() {
        presenter.detachView()
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        if (requireArguments().containsKey("entityId")) {
            requireArguments().getSerializable("entityId")
                ?.let { presenter.onLoaded(it as EntityId) }
        }
        with(binding) {
            btnScanTag.setOnClickListener {
                presenter.readTag()
            }
        }
    }

    override fun showRfidError(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    override fun showLoading(show: Boolean) =
        with(binding) {
            progress.isVisible = show
        }

    override fun openWorkflow(entityId: EntityId) {
        val bundle = Bundle()
        bundle.putSerializable("entityId", entityId)
        findNavController().navigate(R.id.workFlowFragment, bundle, navOptions {
            popUpTo(R.id.attachTagFragment) {
                inclusive = true
            }
        })
    }

    override fun showMessage(message: String) {
        binding.textMessage.text = message
    }
}