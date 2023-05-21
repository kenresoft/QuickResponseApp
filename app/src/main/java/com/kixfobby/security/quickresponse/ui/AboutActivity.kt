package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R

class AboutActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        init()
    }

    private fun init() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        findViewById<View>(R.id.button_feedback).setOnClickListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.button_feedback) {
            toast(this, "Feedback")
        }
    }
}