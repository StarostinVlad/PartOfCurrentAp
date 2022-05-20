package com.starostinvlad.tsdapp.adapter.fingerprints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.BaseViewHolder
import com.starostinvlad.tsdapp.adapter.Item
import com.starostinvlad.tsdapp.adapter.ItemFingerprint
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.Name
import com.starostinvlad.tsdapp.databinding.ItemWorkflowBinding

class WorkFlowFingerprint(private val onSelect: (Entity) -> Unit) :
    ItemFingerprint<ItemWorkflowBinding, Entity> {
    override fun isRelativeItem(item: Item): Boolean = item is Entity

    override fun getLayoutId(): Int = R.layout.item_workflow

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ItemWorkflowBinding, Entity> {
        val binding = ItemWorkflowBinding.inflate(layoutInflater, parent, false)
        return ItemWorkflowViewHolder(binding, onSelect)
    }
}

class ItemWorkflowViewHolder(binding: ItemWorkflowBinding, val onSelect: (Entity) -> Unit) :
    BaseViewHolder<ItemWorkflowBinding, Entity>(binding) {


    override fun onBind(item: Entity) = with(binding) {
        textItemWorkflow.text = item.latest.entityField?.get("name")?.value
        binding.root.setOnClickListener {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            onSelect(item)
        }
    }

}
