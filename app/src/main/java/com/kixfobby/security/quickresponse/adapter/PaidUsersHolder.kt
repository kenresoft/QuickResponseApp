package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.kixfobby.security.quickresponse.databinding.ItemPaidUserBinding
import com.kixfobby.security.quickresponse.model.PaidUsers


class PaidUsersHolder(mView: View) : RecyclerView.ViewHolder(mView), View.OnClickListener, Comparator<PaidUsers> {
    var mContext: Context = mView.context
    var ref: DatabaseReference? = null
    var mAuth: FirebaseAuth? = null
    var user1: FirebaseUser? = null
    var binding = ItemPaidUserBinding.bind(mView)

    fun bindMessage(msg: PaidUsers) {
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser

        binding.email.text = msg.email
        binding.phone.text = msg.phone
        binding.date.text = msg.date
    }

    override fun onClick(p0: View?) {
        Toast.makeText(mContext, "clicked!", Toast.LENGTH_SHORT).show()
    }

    override fun compare(a: PaidUsers, b: PaidUsers): Int {
        return b.date!!.compareTo(a.date!!)
        //return p0.date - p1.date
    }
}