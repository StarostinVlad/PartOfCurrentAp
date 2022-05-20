package com.starostinvlad.tsdapp.workflow_screen

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.WorkFlowFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.data.FromTo
import com.starostinvlad.tsdapp.data.Name
import com.starostinvlad.tsdapp.databinding.WorkflowFragmentBinding
import javax.inject.Inject

class WorkFlowFragment : Fragment(R.layout.workflow_fragment), WorkFlowFragmentContract {
    private val binding: WorkflowFragmentBinding by viewBinding()

    @Inject
    lateinit var presenter: WorkFlowFragmentPresenter
    lateinit var adapter: FingerprintAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        activity?.setActionBar(binding.toolbarWorkflowFragment)
        presenter.attachView(this)

        if (requireArguments().containsKey("entityId")) {
            requireArguments().getSerializable("entityId")
                ?.let { presenter.onLoaded(it as EntityId) }
        }
        adapter = FingerprintAdapter(WorkFlowFingerprint {
            presenter.onSelectedItem(it)
        })
        binding.rvWorkFlow.adapter = adapter

        binding.btnEndTask.setOnClickListener {
            presenter.onEndTaskClick()
        }
        binding.cardDefects.setOnClickListener {
            findNavController().navigate(R.id.defectsFragment)
        }
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun showResult(result: List<Entity>) {
        adapter.setItems(result)
    }

    override fun setTitle(title: String) {
        binding.toolbarWorkflowFragment.title = title
        activity?.actionBar?.title = title
        activity?.setActionBar(binding.toolbarWorkflowFragment)
    }

    override fun openCheckList(item: Entity) {
        val bundle = Bundle()
        bundle.putSerializable("Entity", item)
        findNavController().navigate(
            R.id.checkListFragment,
            bundle
        )
    }

    override fun showError(message: String) {
        binding.textWorkflowError.isVisible = true
        binding.textWorkflowError.text = "$message"
        binding.workFlowFragmentContainer.isVisible = false
    }

    override fun showLoading(show: Boolean) {
        binding.progressWorkflow.isVisible = show
        binding.workFlowFragmentContainer.isVisible = !show
    }

    override fun showToast(mqttHost: String) {
        Toast.makeText(context, mqttHost, Toast.LENGTH_LONG).show()
    }

    override fun openConfirmLocationScreen() {
        findNavController().navigate(R.id.confirmSiteRowFragment, null, navOptions {
            popUpTo(R.id.workFlowFragment) {
                inclusive = true
            }
        })
    }

    override fun showAcceptance(message: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Внимание!")
            .setMessage(message)
            .setCancelable(true)
            .setPositiveButton("Принять") { _, _ ->
                presenter.onSubmitBtnClick()
            }
            .setNegativeButton("Не принимать") { _, _ ->
                presenter.onDeclineBtnClick()
            }
            .setNeutralButton(
                "Отменить"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.show()
    }

    override fun returnToHomeScreen() {
        findNavController().navigate(R.id.operationsFragment, null, navOptions {
            popUpTo(R.id.workFlowFragment) {
                inclusive = true
            }
        })
    }
}