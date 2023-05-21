package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.ContactBase
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.storage.ContactManager.getSavedPersons

class UserAdapter(private val ctx: Context, private val users: List<User>) :
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
            val us = users[position]
            view.number.text = us.email
            view.message.text = us.name
            view.dt.text = us.phone
            view.lyt_parent.setOnClickListener { view ->
                val items: List<ContactBase> = getSavedPersons(ctx)
                val phone = items[0].number
                val ph1 = us.phone

                //Toast.makeText(ctx, ph1, Toast.LENGTH_SHORT).show();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, us, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: User?, position: Int)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var lyt_parent: View
        var number: TextView
        var message: TextView
        var dt: TextView
        var btnRemove: ImageButton

        init {
            number = v.findViewById(R.id.sms_item_number)
            message = v.findViewById(R.id.sms_item_message)
            dt = v.findViewById(R.id.sms_item_dt)
            btnRemove = v.findViewById(R.id.sms_item_remove)
            lyt_parent = v.findViewById(R.id.lyt_parent)
        }
    }
}