package com.starostinvlad.tsdapp.operations_screen

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.databinding.FragmentOperationsBinding
import javax.inject.Inject

class OperationsFragment : Fragment(R.layout.fragment_operations), OperationsFragmentContract,
    View.OnClickListener {
    private val binding: FragmentOperationsBinding by viewBinding()

    @Inject
    lateinit var presenter: OperationsFragmentPresenter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            activity?.setActionBar(binding.toolbarOperations)

            inventoryBtn.setOnClickListener(this@OperationsFragment)
            acceptanceBtn.setOnClickListener(this@OperationsFragment)
            moveBtn.setOnClickListener(this@OperationsFragment)
            shipmentBtn.setOnClickListener(this@OperationsFragment)
            serviceBtn.setOnClickListener(this@OperationsFragment)
        }
        presenter.attachView(this)
        presenter.onLoaded()
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            showDialog()
        }
        callback.isEnabled = true
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Внимание!")
            .setMessage("Выйти изучётной записи?")
            .setCancelable(true)
            .setPositiveButton("Выйти") { _, _ ->
                presenter.onLogoutBtnCLick()
            }
            .setNegativeButton(
                "Отменить"
            ) { dialog, _ ->
                dialog.dismiss()
            }
        builder.show()
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onClick(v: View?) {
        if (v is Button)
            when (v.id) {
                R.id.inventoryBtn -> showToast(v.text.toString())
                R.id.acceptanceBtn -> openAcceptanceScreen()
                R.id.moveBtn -> showToast(v.text.toString())
                R.id.shipmentBtn -> showToast(v.text.toString())
                R.id.serviceBtn -> showToast(v.text.toString())
            }
    }

    private fun openAcceptanceScreen() {
        findNavController().navigate(R.id.inputChassisNumberFragment)
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun openWorkFlow(entityId: EntityId) {
        val bundle = Bundle()
        bundle.putSerializable("entityId", entityId)
        findNavController().navigate(R.id.attachTagFragment, bundle, navOptions {
            popUpTo(R.id.operationsFragment) {
                inclusive = true
            }
        })
    }

    override fun showIncompleteTask(entity: Entity) {
        with(binding) {
            containerCurrentTask.isVisible = true
            containerOperations.isVisible = false
            entity.latest.entityField?.let { entityField ->
                txtCurrentTask.text = entityField["name"]?.value
                acceptTaskBtn.setOnClickListener {
                    presenter.onAcceptedTask(entity.entityId)
                    containerOperations.isVisible = true
                }
                declineTaskBtn.setOnClickListener {
                    presenter.onDeclinedTask(entityField["name"]!!.value)
                }
            }
        }
    }

    override fun hideIncompletedTask() {
        binding.containerCurrentTask.isVisible = false
        binding.containerOperations.isVisible = true
    }

    override fun showError(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
    }

    override fun logout() {
        findNavController().navigate(R.id.loginFragment, null, navOptions {
            popUpTo(R.id.operationsFragment) {
                inclusive = true
            }
        })
    }

    override fun openConfirmLocation(entityId: EntityId) {
        val bundle = Bundle()
        bundle.putSerializable("entityId", entityId)
        findNavController().navigate(R.id.confirmLocationFragment, bundle, navOptions {
            popUpTo(R.id.operationsFragment) {
                inclusive = true
            }
        })
    }
}