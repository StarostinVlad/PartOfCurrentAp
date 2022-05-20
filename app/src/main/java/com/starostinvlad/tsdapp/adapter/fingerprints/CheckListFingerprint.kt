package com.starostinvlad.tsdapp.adapter.fingerprints

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.BaseViewHolder
import com.starostinvlad.tsdapp.adapter.Item
import com.starostinvlad.tsdapp.adapter.ItemFingerprint
import com.starostinvlad.tsdapp.data.CheckBoxListItem
import com.starostinvlad.tsdapp.databinding.ItemChecklistBinding

class CheckListFingerprint() :
    ItemFingerprint<ItemChecklistBinding, CheckBoxListItem> {
    override fun isRelativeItem(item: Item): Boolean = item is CheckBoxListItem

    override fun getLayoutId(): Int = R.layout.item_checklist

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ItemChecklistBinding, CheckBoxListItem> {
        val binding = ItemChecklistBinding.inflate(layoutInflater, parent, false)
        return ItemCheckListViewHolder(binding)
    }
}

class ItemCheckListViewHolder(
    binding: ItemChecklistBinding
) :
    BaseViewHolder<ItemChecklistBinding, CheckBoxListItem>(binding) {

    private val TAG = javaClass.simpleName

    override fun onBind(item: CheckBoxListItem) = with(binding) {
        val checkListener =
            CompoundButton.OnCheckedChangeListener { button, checked ->
                if (checked)
                    when (button) {
                        toggleCancelled -> {
                            toggleSuccess.isChecked = false
                            item.status = 0
                        }
                        toggleSuccess -> {
                            toggleCancelled.isChecked = false
                            item.status = 1
                        }
                    }
                else
                    if (!toggleCancelled.isChecked && !toggleSuccess.isChecked)
                        item.status = -1
            }
        toggleCancelled.setOnCheckedChangeListener(checkListener)
        toggleSuccess.setOnCheckedChangeListener(checkListener)
        titleCheckBoxItem.text = item.key
        subTitleCheckBoxItem.text = item.value
        Log.d(TAG, "onBind: value: ${item.status}")
        when (item.status) {
            -1 -> {
                toggleCancelled.isChecked = false
                toggleSuccess.isChecked = false
            }
            0 -> {
                toggleCancelled.isChecked = true
                toggleSuccess.isChecked = false
            }
            1 -> {
                toggleSuccess.isChecked = true
                toggleCancelled.isChecked = false
            }
        }

    }

}
