package com.kixfobby.security.quickresponse.helper.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.util.Calendar
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.EXTRA_NOTIFICATION_ID
import androidx.core.app.TaskStackBuilder
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.receiver.AlarmReceiver
import com.kixfobby.security.quickresponse.storage.Pref


object NotificationScheduler {
    private const val DAILY_REMINDER_REQUEST_CODE = 100
    const val TAG = "NotificationScheduler"
    fun setReminder(context: Context, cls: Class<*>?, snooze: Boolean, duration: Long) {
        var t = Calendar.getInstance()
        t.timeInMillis = System.currentTimeMillis()
        val startTime = t.timeInMillis + duration

        // Enable a receiver
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, cls!!), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmIntent = Intent(context, cls)
        alarmIntent.putExtra("reminder", "BIG RECEIVER SERVICE HERE!")
        //alarmIntent.putExtra("snooze", snooze)
        Pref(context).put("snooze", snooze)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        /*am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + snooze,
            duration,
            pendingIntent
        )*/

        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
        } else {
            if (currentapiVersion < Build.VERSION_CODES.M) {
                am.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
            } else {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    pendingIntent
                )
            }
        }

    }

    fun cancelReminder(context: Context) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, AlarmReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showNotification(context: Context, cls: Class<*>?, title: String?, content: String?) {
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)/////// change tone here
        val soundUri: Uri = Uri.parse(
            "android.resource://" + context.getApplicationContext().getPackageName() + "/" + R.raw.alarm
        )

        val notificationIntent = Intent(context, cls)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(cls!!)
        stackBuilder.addNextIntent(notificationIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
            DAILY_REMINDER_REQUEST_CODE,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationChannelId = "ALARMCHANNELID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                notificationChannelId,
                "Alarm Service",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Alarm Channel"
                it.enableLights(true)
                it.setSound(alarmSound, audioAttributes)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra(EXTRA_NOTIFICATION_ID, "ACTION_SNOOZE")
        }

        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(context, 0, snoozeIntent, 0)


        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(false)
            .setAllowSystemGeneratedContextualActions(false)
            .setSound(alarmSound)
            .setSmallIcon(R.drawable.ic_baseline_alarm_add_24)
            .setTicker("aaa")
            //.setTimeoutAfter(3000L)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.ic_baseline_alarm_off_24, context.getString(R.string.snooze), snoozePendingIntent)
            .setContentIntent(pendingIntent).build()

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification)
    }

    fun cancelNotification(context: Context) {
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(DAILY_REMINDER_REQUEST_CODE)
    }


    fun setRestarter(context: Context, cls: Class<*>?, snooze: Boolean, duration: Long) {

        val startTime = System.currentTimeMillis() + duration
        // Enable a receiver
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, cls!!), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmIntent = Intent(context, cls)
        alarmIntent.putExtra("reminder", "BIG RECEIVER SERVICE HERE!")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            DAILY_REMINDER_REQUEST_CODE,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
        /*am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startTime,
            duration,
            pendingIntent
        )*/

        val currentapiVersion = Build.VERSION.SDK_INT
        if (currentapiVersion < Build.VERSION_CODES.KITKAT) {
            am.set(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
        } else {
            if (currentapiVersion < Build.VERSION_CODES.M) {
                am.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent)
            } else {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    startTime,
                    pendingIntent
                )
                //Log.i("Restarter : ", " Broadcast, Service tried to stop...")
            }
        }

    }
}