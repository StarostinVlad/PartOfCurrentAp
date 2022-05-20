package com.starostinvlad.tsdapp.checklist_screen

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.CheckListFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.CheckBoxListItem
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.databinding.ChecklistFragmentBinding
import javax.inject.Inject


class CheckListFragment : Fragment(R.layout.checklist_fragment), CheckListFragmentContract {
    private val binding: ChecklistFragmentBinding by viewBinding()
    var adapter: FingerprintAdapter? = null

    @Inject
    lateinit var presenter: CheckListFragmentPresenter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)

        if (requireArguments().containsKey("Entity"))
            presenter.onLoaded(
                requireArguments().getSerializable("Entity") as Entity
            )
        adapter =
            FingerprintAdapter(
                CheckListFingerprint()
            )

        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presenter.onUploadBtnClick()
        }
        callback.isEnabled = true

        binding.rvCheckList.adapter = adapter

        binding.btnUploadCheckList.setOnClickListener {
            presenter.onUploadBtnClick()
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun showResult(result: List<CheckBoxListItem>) {
        adapter?.setItems(result)
        binding.btnUploadCheckList.isEnabled = true
    }


    override fun requestSucces() {
        findNavController().popBackStack()
    }

    override fun showError(message: String?) {
        binding.textMessage.text = "$message"
    }

    override fun showLoading(show: Boolean) {
        with(binding) {
            progressCheckList.isVisible = show
            containerCheckListFragment.isVisible = !show
        }
    }

    override fun showNotCompleteDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Внимание!")
            .setMessage("Не все поля чек-листа заполнены")
            .setCancelable(true)
            .setPositiveButton("Завершить") { _, _ ->
                presenter.onSubmitBtnClick()
            }
            .setNegativeButton(
                "Отменить"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.show()
    }
}