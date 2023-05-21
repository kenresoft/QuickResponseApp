package com.kixfobby.security.quickresponse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.kixfobby.security.quickresponse.helper.alarm.NotificationScheduler
import com.kixfobby.security.quickresponse.service.BaseService
import com.kixfobby.security.quickresponse.service.UpdateService

////KENNETH'S CODE
class Restarter : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, BaseService::class.java))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, UpdateService::class.java))
            Log.i("Restarter : ", " Service restarted1")
            /*var i: Intent = Intent(context, RestarterActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)*/
        } else {
            context.startService(Intent(context, UpdateService::class.java))
            Log.i("Restarter : ", " Service restarted2")
        }

        NotificationScheduler.setRestarter(context, Restarter::class.java, false, 0)
        Log.i("Restarter : ", " Broadcast, Service tried to stop")
    }
}