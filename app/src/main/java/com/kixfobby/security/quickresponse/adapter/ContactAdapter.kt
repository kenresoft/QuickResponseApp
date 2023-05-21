package com.kixfobby.security.quickresponse.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.room.entity.ContactEntity

class ContactAdapter : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contactList = emptyList<ContactEntity>()

    class ContactViewHolder(itemViews: View) : RecyclerView.ViewHolder(itemViews) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentItem = contactList[position]
        holder.itemView.findViewById<TextView>(R.id.contact_item_name).text = currentItem.name
        holder.itemView.findViewById<TextView>(R.id.contact_item_number).text = currentItem.number
        return
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    fun setContact(contactList: List<ContactEntity>){
        this.contactList = contactList
        notifyDataSetChanged()
    }

    fun restoreContact(contactList: List<ContactEntity>){
        this.contactList = contactList
        notifyDataSetChanged()
    }
}