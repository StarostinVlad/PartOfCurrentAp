package com.starostinvlad.tsdapp.acceptance_screen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.databinding.FragmentInputChassisNumberBinding
import javax.inject.Inject

class InputChassisNumberFragment : Fragment(R.layout.fragment_input_chassis_number),
    InputChassisNumberFragmentContract {
    val binding: FragmentInputChassisNumberBinding by viewBinding()

    @Inject
    lateinit var presenter: InputChassisNumberFragmentPresenter

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
        with(binding) {
            btnFindChassis.setOnClickListener {
                chassisNumberInputStyle.error = null
                presenter.onFindBtnClick(editTextChassisNumber.text.toString())
            }
            btnScanTag.setOnClickListener {
//                findNavController().navigate(R.id.myCustomDialog)
                presenter.readTag()
            }
        }
    }

    override fun showTaskList(taskListId: EntityId, fromRfid: Boolean) {
        val bundle = Bundle()
        bundle.putSerializable("taskListId", taskListId)
        bundle.putBoolean("fromRfid", fromRfid)
        findNavController().navigate(R.id.taskListFragment, bundle)
    }

    override fun showError(message: String) {
        binding.chassisNumberInputStyle.error = message
    }

    override fun showRfidError(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    override fun showLoading(show: Boolean) =
        with(binding) {
            progress.isVisible = show
            containerChassisNumber.isVisible = !show
        }
}
//
//class CustomDialogWithInstruction : DialogFragment(R.layout.dialog_fragment) {
//    val binding: DialogFragmentBinding by viewBinding()
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        with(binding) {
//            btnCloseInstruction.setOnClickListener {
//                dialog?.dismiss()
//            }
//            Glide
//                .with(this@CustomDialogWithInstruction)
//                .load(R.drawable.instruction)
//                .centerCrop()
//                .into(imageInstruction)
//        }
//    }
//
//}