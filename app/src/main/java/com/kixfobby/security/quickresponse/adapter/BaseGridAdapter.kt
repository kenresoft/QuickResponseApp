package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.badge.ImageBadgeView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.DashItem

class BaseGridAdapter(private val ctx: Context, private val items: List<DashItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard, parent, false)
        vh = OriginalViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val p = items[position]
            holder.title.text = p.title
            holder.image.setImageDrawable(ContextCompat.getDrawable(ctx, p.image))
            /*if (position == 2) {
                holder.badge.visibility = View.VISIBLE
                //holder.badge.badgeValue = p.badge.toString()
            }*/
            holder.lytParent.setOnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, items[position], position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: DashItem?, position: Int)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var image: ImageView = v.findViewById(R.id.image)
        var title: TextView = v.findViewById(R.id.title)
        //var badge: ImageBadgeView = v.findViewById(R.id.badge)
        var lytParent: View = v.findViewById(R.id.lyt_parent)

    }
}