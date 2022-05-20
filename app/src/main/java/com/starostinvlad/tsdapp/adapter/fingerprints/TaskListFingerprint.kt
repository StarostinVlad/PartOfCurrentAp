package com.starostinvlad.tsdapp.adapter.fingerprints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.BaseViewHolder
import com.starostinvlad.tsdapp.adapter.Item
import com.starostinvlad.tsdapp.adapter.ItemFingerprint
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.databinding.ItemTaskBinding

class TaskListFingerprint(private val onSelectItem: (Entity) -> Unit) :
    ItemFingerprint<ItemTaskBinding, Entity> {
    override fun isRelativeItem(item: Item): Boolean = item is Entity

    override fun getLayoutId(): Int = R.layout.item_task

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ItemTaskBinding, Entity> {
        val binding = ItemTaskBinding.inflate(layoutInflater, parent, false)
        return ItemTaskViewHolder(binding, onSelectItem)
    }
}

class ItemTaskViewHolder(
    binding: ItemTaskBinding,
    val onSelectItem: (Entity) -> Unit
) :
    BaseViewHolder<ItemTaskBinding, Entity>(binding) {

    private val TAG = javaClass.simpleName

    override fun onBind(item: Entity) = with(binding) {
        textItemTask.text = item.latest.entityField!!["name"]?.value
        root.setOnClickListener {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            onSelectItem(item)
        }
    }

}
