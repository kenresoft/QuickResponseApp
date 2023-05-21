package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.View
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.databinding.ItemExpandBinding
import com.kixfobby.security.quickresponse.model.AdminMsg
import com.kixfobby.security.quickresponse.util.Tools
import com.kixfobby.security.quickresponse.util.ViewAnimation


class AdapterListExpand(var mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener {
    var mContext: Context = mView.context
    var ref: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    var user1: FirebaseUser? = null
    var binding = ItemExpandBinding.bind(mView)

    fun bindMessage(msg: AdminMsg) {
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        ref = FirebaseDatabase.getInstance().getReference("Admin").child("Message").child(user1!!.uid)

        binding.title.text = msg.title
        binding.message.text = msg.message
        binding.time.text = msg.time

        if (msg.expanded == true) {
            binding.lytExpand.visibility = View.VISIBLE
            //msg.setSeen(true)
        } else {
            binding.lytExpand.visibility = View.GONE
        }

        msg.expanded?.let { Tools.toggleArrow(it, binding.btExpand, false) }
        binding.btExpand.setOnClickListener(View.OnClickListener { v ->
            val show = toggleLayoutExpand(!msg.expanded!!, v, binding.lytExpand)
            msg.expanded = show
        })

    }

    private fun toggleLayoutExpand(show: Boolean, view: View, lyt_expand: View): Boolean {
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
        /*val message: ArrayList<AdminMsg?> = ArrayList<AdminMsg?>()
        
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
        })*/
        ref?.child(layoutPosition.plus(1).toString())
            ?.setValue(AdminMsg().setTitle("QWE").setMessage("RTY").setTime("UIO"))
    }

    companion object {
        private const val MAX_WIDTH = 200
        private const val MAX_HEIGHT = 200
    }

    init {
        mView.setOnClickListener(this)
    }
}