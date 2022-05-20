package com.starostinvlad.tsdapp.tasklist_screen

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.TaskListFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.EntityData
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.databinding.FragmentInputChassisNumberBinding
import com.starostinvlad.tsdapp.databinding.FragmentTaskListBinding
import javax.inject.Inject

class TaskListFragment : Fragment(R.layout.fragment_task_list),
    TaskListFragmentContract {
    val binding: FragmentTaskListBinding by viewBinding()

    var adapter: FingerprintAdapter? = null

    @Inject
    lateinit var presenter: TaskListFragmentPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.appComponent?.inject(this)
    }

    override fun onDestroyView() {
        presenter.detachView()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        with(binding) {
            adapter =
                FingerprintAdapter(
                    TaskListFingerprint { entity ->
                        presenter.onItemClick(entity)
                    }
                )

            foundedTaskRV.adapter = adapter
        }
        var fromRfid = false
        if (requireArguments().containsKey("fromRfid")) {
            fromRfid = requireArguments().getBoolean("fromRfid")
        }
        if (requireArguments().containsKey("taskListId")) {
            requireArguments().getSerializable("taskListId")
                ?.let { presenter.onLoaded(it as EntityId, fromRfid) }
        }
    }

    override fun openTask(entityId: EntityId) {
        val bundle = Bundle()
        bundle.putSerializable("entityId", entityId)
        findNavController().navigate(R.id.workFlowFragment, bundle)
    }

    override fun showTaskList(taskList: List<Entity>) {
        Log.e("showTaskList", "taskList: show")
        with(binding) {
            foundedTaskRV.isVisible = true
            adapter?.setItems(taskList)
            Log.e("showTaskList", "taskList: showed")
        }
    }

    override fun showError(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
    }

    override fun showLoading(show: Boolean) =
        with(binding) {
            progress.isVisible = show
            foundedTaskRV.isVisible = !show
        }

    override fun openAcceptanceTask(entityId: EntityId) {
        val bundle = Bundle()
        bundle.putSerializable("entityId", entityId)
        findNavController().navigate(R.id.attachTagFragment, bundle)
    }
}