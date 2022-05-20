package com.starostinvlad.tsdapp.current_task_fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.FingerprintAdapter
import com.starostinvlad.tsdapp.adapter.fingerprints.TaskListFingerprint
import com.starostinvlad.tsdapp.adapter.fingerprints.WorkFlowFingerprint
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.data.EntityId
import com.starostinvlad.tsdapp.databinding.FragmentCurrentTaskBinding
import javax.inject.Inject

class CurrentTaskFragment : Fragment(R.layout.fragment_current_task), CurrentTaskFragmentContract {

    val binding: FragmentCurrentTaskBinding by viewBinding()
    lateinit var adapter: FingerprintAdapter

    val TAG = javaClass.simpleName

    @Inject
    lateinit var presenter: CurrentTaskFragmentPresenter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        context?.appComponent?.inject(this)
        super.onViewCreated(view, savedInstanceState)
        activity?.setActionBar(binding.toolbarCurrentTask)
        presenter.attachView(this)
        if (requireArguments().containsKey("entityId")) {
            requireArguments().getSerializable("entityId")
                ?.let { presenter.onLoaded(it as EntityId) }
        }

        adapter = FingerprintAdapter(listOf(TaskListFingerprint {
            Log.d(TAG, "onViewCreated: $it")
//            presenter.onSelectedItem(it)
        }))
        binding.tasksRV.adapter = adapter

    }
}