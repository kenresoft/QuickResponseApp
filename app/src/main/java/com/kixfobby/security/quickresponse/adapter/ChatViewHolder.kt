package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ChatItemListBinding
import com.kixfobby.security.quickresponse.model.Message
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.ui.ChatRoomActivity


class ChatViewHolder(mView: View) : RecyclerView.ViewHolder(mView), View.OnLongClickListener {
    var mContext: Context = mView.context
    var ref: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    var user1: FirebaseUser? = null
    var message: Message? = null
    var binding = ChatItemListBinding.bind(mView)

    fun bindMessage(msg: Message) {
        message = msg
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser


        ref = FirebaseDatabase.getInstance().getReferenceFromUrl(Pref(mContext).get("mFriend", "mFriend").toString())
        //ref = FirebaseDatabase.getInstance().getReference("Admin").child("Message").child(user1!!.uid)

        binding.otherMessage.text = msg.message
        binding.otherName.text = msg.senderName
        binding.otherDate.text = msg.date

        /*if (msg.expanded == true) {
            binding.lytExpand.visibility = View.VISIBLE
            //msg.setSeen(true)
        } else {
            binding.lytExpand.visibility = View.GONE
        }

        msg.expanded?.let { Tools.toggleArrow(it, binding.btExpand, false) }
        binding.btExpand.setOnClickListener(View.OnClickListener { v ->
            val show = toggleLayoutExpand(!msg.expanded!!, v, binding.lytExpand)
            msg.expanded = show
        })*/

    }

    /*private fun toggleLayoutExpand(show: Boolean, view: View, lyt_expand: View): Boolean {
        Tools.toggleArrow(show, view)
        if (show) {
            ViewAnimation.expand(lyt_expand)
            //ref?.child(getLayoutPosition().toString())?.child("seen")?.setValue(true)
            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                .putBoolean("${getLayoutPosition()} seen", true).apply()
        } else {
            ViewAnimation.collapse(lyt_expand)
        }
        return show
    }

    override fun onClick(view: View) {
        *//*val message: ArrayList<AdminMsg?> = ArrayList<AdminMsg?>()
        
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    message.add(snapshot.getValue(AdminMsg::class.java))
                }
                val itemPosition: Int = getLayoutPosition()
                val intent = Intent(mContext, MessageActivity::class.java)
                intent.putExtra("position", itemPosition.toString() + "")
                //intent.putExtra("message", Parcels.wrap(message))
                //mContext.startActivity(intent)
                //Toast.makeText(mContext, itemPosition, Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })*//*
        *//*ref?.child(getLayoutPosition().plus(1).toString())
            ?.setValue(AdminMsg().setTitle("QWE").setMessage("RTY").setTime("UIO"))*//*
    }

    companion object {
        private const val MAX_WIDTH = 200
        private const val MAX_HEIGHT = 200
    }*/

    init {
        mView.setOnLongClickListener(this)
    }

    fun onClick(v: View?) {
        var s: String? = message!!.date
        /*ref?.child(s!!)?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(parent: DataSnapshot) {
                val list: MutableList<String?> = ArrayList()
                for (child in parent.children) {
                    val value = child.key
                    list.add(value)
                }
                *//*val set: MutableSet<String?> = HashSet()
                    set.addAll(list)
                    Pref(mContext).put("friends_set", set)*//*
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })*/
    }

    override fun onLongClick(v: View?): Boolean {
        val alert = AlertDialog.Builder(mContext, R.style.CustomDialogTheme)
        alert.setMessage("Delete message?")
        alert.setCancelable(false)
        alert.setPositiveButton(android.R.string.yes) { dialogInterface, i ->
            ref?.child(message!!.date?.trim().toString())?.removeValue { error, ref ->
                if (error == null && ref.equals(null)) {
                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show()
                } else if (error != null) {
                    Toast.makeText(mContext, error.message, Toast.LENGTH_SHORT).show()
                }
                ChatRoomActivity().updateChatList()
            }
        }
        alert.setNegativeButton(android.R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }
        alert.show()
        return false
    }
}