package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.UserAdapter

class EmergencyBaseActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)
        supportActionBar!!.title = "Emergency Numbers"
        val listSmsBase = UserAdapter(applicationContext, retrieveUsers(applicationContext))
        val recyclerView = findViewById<RecyclerView>(R.id.emergency_list)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = false
        //recyclerView.setAdapter(listSmsBase);
        listSmsBase.notifyDataSetChanged()
        Toast.makeText(this, "Currently not available...", Toast.LENGTH_SHORT).show()
    }
}