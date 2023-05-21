package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.ProcessMainClass
import com.kixfobby.security.quickresponse.receiver.RestartServiceBroadcastReceiver.Companion.scheduleJob
import com.kixfobby.security.quickresponse.storage.Constants

class Home : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({ finish() }, Constants.EXIT_DELAY.toLong())
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob(applicationContext)
        } else {
            val bck = ProcessMainClass()
            bck.launchService(applicationContext)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scheduleJob(applicationContext)
        } else {
            val bck = ProcessMainClass()
            bck.launchService(applicationContext)
        }
    }

    override fun onStop() {
        super.onStop()
        val broadcastIntent = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent)
    }
}