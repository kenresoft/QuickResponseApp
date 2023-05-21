package com.kixfobby.security.quickresponse.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.util.Log
import android.widget.Toast
import com.kixfobby.security.quickresponse.helper.alarm.NotificationScheduler
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.ui.MainActivity


class AlarmReceiver : BroadcastReceiver() {
    var TAG = "AlarmReceiver"
    var time: Long? = null

    override fun onReceive(context: Context, intent: Intent) {
        //context.startService(Intent(context, MediaService::class.java))

        val action: String? = intent.action
        val reminder: String? = intent.extras?.getString("reminder")
        //val snooze: Boolean? = intent.extras?.getBoolean("snooze")

        val snooze = Pref(context).get("snooze", false)
        Toast.makeText(context, snooze.toString(), Toast.LENGTH_SHORT).show()

        val duration = Pref(context).get("duration", 1800000L)
        val startTime = Pref(context).get("startTime", 1440L)
        val endTime = Pref(context).get("endTime", 1440L)
        var t = Calendar.getInstance()
        t.timeInMillis = System.currentTimeMillis()

        time = if (snooze) 40000L else t.timeInMillis + duration

        Log.d(TAG, "s: " + startTime.toString())
        Log.d(TAG, "e: " + endTime.toString())
        Log.d(TAG, "c: " + System.currentTimeMillis())
        Log.d(TAG, "d: " + duration.toString())

        if (System.currentTimeMillis() >= endTime) {
            NotificationScheduler.cancelReminder(context)
            NotificationScheduler.cancelNotification(context)
        }

        if (action.equals("ACTION_SNOOZE")) {
            NotificationScheduler.cancelReminder(context)
            NotificationScheduler.cancelNotification(context)

            NotificationScheduler.setReminder(
                context,
                AlarmReceiver::class.java,
                true,
                duration
            )
            NotificationScheduler.showNotification(
                context, MainActivity::class.java,
                "TRAVELLER'S ALARM NOTIFICATION", "Hope you are safe?"
            )
            Log.d(TAG, "onReceive: ACTION_SNOOZE")
            Log.d(TAG, "Snooze:" + snooze.toString())
            return

        } else {
            // MAIN ALARM
            NotificationScheduler.setReminder(
                context,
                AlarmReceiver::class.java,
                snooze,
                duration
            )
            NotificationScheduler.showNotification(
                context, MainActivity::class.java,
                "New Notification", "Check them now?"
            )
            Log.d(TAG, "onReceive: ACTION_MAIN")
            Log.d(TAG, "Snooze:" + snooze.toString())
            return

        }
    }
}