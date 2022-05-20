package com.starostinvlad.tsdapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class FingerprintAdapter(
    private val fingerprints: List<ItemFingerprint<*, *>>,
) : RecyclerView.Adapter<BaseViewHolder<ViewBinding, Item>>() {
    constructor(fingerprints: ItemFingerprint<*, *>) : this(listOf(fingerprints))

    private val items = mutableListOf<Item>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<ViewBinding, Item> {
        val inflater = LayoutInflater.from(parent.context)
        return fingerprints.find { it.getLayoutId() == viewType }
            ?.getViewHolder(inflater, parent)
            ?.let { it as BaseViewHolder<ViewBinding, Item> }
            ?: throw IllegalArgumentException("View type not found: $viewType")
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ViewBinding, Item>, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return fingerprints.find { it.isRelativeItem(item) }
            ?.getLayoutId()
            ?: throw IllegalArgumentException("View type not found: $item")
    }

    private val TAG = javaClass.simpleName
    fun setItems(newItems: List<Item>) {
        items.clear()
        items.addAll(newItems)
        Log.d(TAG, "setItems: $items")
        notifyDataSetChanged()
    }

    fun getItemPosition(item: Item): Int? {
        if (items.contains(item))
            return items.indexOf(item)
        return null
    }
}