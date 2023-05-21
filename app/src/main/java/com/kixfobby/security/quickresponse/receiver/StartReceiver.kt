package com.kixfobby.security.quickresponse.receiver;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.kixfobby.security.quickresponse.service.UpdateService
import com.kixfobby.security.quickresponse.storage.Actions
import com.kixfobby.security.quickresponse.storage.ServiceState
import com.kixfobby.security.quickresponse.storage.getServiceState

class StartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && getServiceState(context) == ServiceState.STARTED) {
            Intent(context, UpdateService::class.java).also {
                it.action = Actions.START.name
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e("StartReceiver: ","Starting the service in >=26 Mode from a BroadcastReceiver")
                    context.startForegroundService(it)
                    return
                }
                //log("Starting the service in < 26 Mode from a BroadcastReceiver")
                context.startService(it)
            }
        }
    }
}
