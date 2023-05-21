package com.kixfobby.security.quickresponse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R

class BillingAdapter : RecyclerView.Adapter<BillingAdapter.ViewHolder>() {
    private val itemCount = 50
    private val backgrounds = IntArray(3)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val index = (Math.random() * 3).toInt()
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.billing_list_item, parent, false), index)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.setBackgroundResource(backgrounds[holder.backgroundIndex])
    }

    override fun getItemCount(): Int {
        return itemCount
    }

    inner class ViewHolder(itemView: View, backgroundIndex: Int) : RecyclerView.ViewHolder(itemView) {
        val view: FrameLayout
        var backgroundIndex = 0

        init {
            this.backgroundIndex = backgroundIndex
            view = itemView.findViewById<View>(R.id.item) as FrameLayout
        }
    }

    init {
        backgrounds[0] = R.color.colorPrimaryLight
        backgrounds[1] = R.color.colorPrimaryNormal
        backgrounds[2] = R.color.colorPrimaryDark
    }
}