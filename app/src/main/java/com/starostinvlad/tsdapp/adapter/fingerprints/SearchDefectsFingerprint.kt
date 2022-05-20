package com.starostinvlad.tsdapp.adapter.fingerprints

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.adapter.BaseViewHolder
import com.starostinvlad.tsdapp.adapter.Item
import com.starostinvlad.tsdapp.adapter.ItemFingerprint
import com.starostinvlad.tsdapp.data.DefectData
import com.starostinvlad.tsdapp.data.Entity
import com.starostinvlad.tsdapp.data.Name
import com.starostinvlad.tsdapp.databinding.ItemDefectBinding
import com.starostinvlad.tsdapp.databinding.ItemWorkflowBinding

class SearchDefectsFingerprint(private val onSelect: (DefectData) -> Unit) :
    ItemFingerprint<ItemDefectBinding, DefectData> {
    override fun isRelativeItem(item: Item): Boolean = item is DefectData

    override fun getLayoutId(): Int = R.layout.item_defect

    override fun getViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup
    ): BaseViewHolder<ItemDefectBinding, DefectData> {
        val binding = ItemDefectBinding.inflate(layoutInflater, parent, false)
        return ItemSearchDefectViewHolder(binding, onSelect)
    }
}

class ItemSearchDefectViewHolder(binding: ItemDefectBinding, val onSelect: (DefectData) -> Unit) :
    BaseViewHolder<ItemDefectBinding, DefectData>(binding) {


    override fun onBind(item: DefectData) = with(binding) {
        titleItemDefect.text = item.title
        subTitleItemDefect.text = item.subTitle

        btnChangeDefectStatus.isEnabled = !item.status

        btnChangeDefectStatus.setOnClickListener {
            if (bindingAdapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
            onSelect(item)
            item.status = !item.status
            when (item.status) {
                false -> btnChangeDefectStatus.text = "Добавить"
                true -> btnChangeDefectStatus.text = "Добавлен"
            }
        }

        when (item.status) {
            false -> btnChangeDefectStatus.text = "Добавить"
            true -> btnChangeDefectStatus.text = "Добавлен"
        }
    }

}
