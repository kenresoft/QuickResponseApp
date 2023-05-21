package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.ChatBase
import com.kixfobby.security.quickresponse.storage.ChatBaseManager.removeChat

class ChatBaseAdapter(private val ctx: Context, private val items: ArrayList<ChatBase>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.smsbase_item, parent, false)
        vh = OriginalViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val view = holder
            val sm = items[position]
            view.name.text = sm.name
            view.number.text = sm.number
            view.message.text = sm.message
            view.dt.text = sm.getDT()
            view.btnRemove.setOnClickListener {
                removeChat(ctx, sm)
                items.removeAt(position)
                notifyDataSetChanged()
            }
            view.lyt_parent.setOnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, sm, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: ChatBase?, position: Int)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var lyt_parent: View = v.findViewById(R.id.lyt_parent)
        var name: TextView = v.findViewById(R.id.sms_item_name)
        var number: TextView = v.findViewById(R.id.sms_item_number)
        var message: TextView = v.findViewById(R.id.sms_item_message)
        var dt: TextView = v.findViewById(R.id.sms_item_dt)
        var btnRemove: ImageButton = v.findViewById(R.id.sms_item_remove)

    }
}