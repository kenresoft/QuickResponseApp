package com.kixfobby.security.quickresponse.service

import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.kixfobby.security.quickresponse.BuildConfig
import com.kixfobby.security.quickresponse.ProcessMainClass
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.util.Log.v
import com.kixfobby.security.quickresponse.widget.Notification
import org.jetbrains.annotations.Contract
import java.util.*

class SmsMonitorService : Service() {
    var oldTime: Long = 0
    private var counter = 0
    private val crSMS: ContentResolver? = null

    //private SmsContentObserver observerSMS = null;
    private var context: Context? = null
    override fun onCreate() {
        super.onCreate()
        context = this.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            restartForeground()
        }
        mCurrentService = this
        if (BuildConfig.DEBUG) v("SmsMonitorService created")
        //registerSMSObserver();
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "restarting Service !!")
        counter = 0

        // it has been killed by Android and now it is restarted. We must make sure to have reinitialised everything
        if (intent == null) {
            val bck = ProcessMainClass()
            bck.launchService(this)
        }

        // make sure you call the startForeground on onStartCommand because otherwise
        // when we hide the notification on onScreen it will nto restart in Android 6 and 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            restartForeground()
        }
        startTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy called")
        //unregisterSMSObserver();
        // restart the never ending service
        val broadcastIntent = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent)
        stoptimertask()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    /*
     * Registers the observer for SMS changes
     */
    /* private void registerSMSObserver() {
        if (observerSMS == null) {
            observerSMS = new SmsContentObserver(new Handler());
            crSMS = getContentResolver();
            crSMS.registerContentObserver(uriSMS, true, observerSMS);
            if (BuildConfig.DEBUG) Log.v("SMS Observer registered.");
        }
    }*/
    /**
     * Unregisters the observer for call log changes
     */
    /*private void unregisterSMSObserver() {
        if (crSMS != null) {
            crSMS.unregisterContentObserver(observerSMS);
        }
        if (observerSMS != null) {
            observerSMS = null;
        }
        if (BuildConfig.DEBUG) Log.v("Unregistered SMS Observer");
    }*/
    fun restartForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "restarting foreground")
            try {
                val notification = Notification()
                startForeground(
                    NOTIFICATION_ID,
                    notification.setNotification(
                        this,
                        "Service notification",
                        "This is the service's notification",
                        R.drawable.baseline_notification_important_24
                    )
                )
                Log.i(TAG, "restarting foreground successful")
                startTimer()
            } catch (e: Exception) {
                Log.e(TAG, "Error in notification " + e.message)
            }
        }
    }

    ////KENNETH'S CODE
    override fun onTaskRemoved(rootIntent: Intent) {
        Log.i(TAG, "onTaskRemoved called")
        // restart the never ending service
        val broadcastIntent = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent)
        // do not call stoptimertask because on some phones it is called asynchronously
        // after you swipe out the app and therefore sometimes
        // it will stop the timer after it was restarted
        // stoptimertask();
        Toast.makeText(baseContext, "Removed Task", Toast.LENGTH_LONG).show()
        //startActivity(new Intent(getBaseContext(), Home.class));
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        super.onTaskRemoved(rootIntent)
    }

    fun startTimer() {
        Log.i(TAG, "Starting timer")

        //set a new Timer - if one is already running, cancel it to avoid two running at the same time
        stoptimertask()
        timer = Timer()

        //initialize the TimerTask's job
        initializeTimerTask()
        Log.i(TAG, "Scheduling...")
        //schedule the timer, to wake up every 1 second
        timer!!.schedule(timerTask, 1000, 1000) //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    fun initializeTimerTask() {
        Log.i(TAG, "initialising TimerTask")
        timerTask = object : TimerTask() {
            override fun run() {
                Log.i("in timer", "in timer ++++  " + counter++)
            }
        }
    }

    /**
     * not needed
     */
    fun stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    } /*private class SmsContentObserver extends ContentObserver {
        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Cursor c = context.getContentResolver().query(SMS_CONTENT_URI,
            // null, "read = 0", null, null);
            int count = SmsPopupUtils.getUnreadMessagesCount(context);
            if (BuildConfig.DEBUG) Log.v("getUnreadCount = " + count);
            if (count == 0) {
                ManageNotification.clearAll(context);
                finishStartingService(SmsMonitorService.this);
            } else {
                // TODO: do something with count>0, maybe refresh the
                // notification
            }
        }
    }*/

    companion object {
        // private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
        // private static final Uri SMS_INBOX_CONTENT_URI =
        // Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
        protected const val NOTIFICATION_ID = 1337
        private val uriSMS = Uri.parse("content://mms-sms/conversations/")
        private const val TAG = "SmsMonitorService"
        private var mCurrentService: SmsMonitorService? = null

        /**
         * static to avoid multiple timers to be created when the service is called several times
         */
        private var timer: Timer? = null
        private var timerTask: TimerTask? = null

        /**
         * Start the service to process that will run the content observer
         */
        fun beginStartingService(context: Context) {
            if (BuildConfig.DEBUG) v("SmsMonitorService: beginStartingService()")
            context.startService(Intent(context, SmsMonitorService::class.java))
        }

        /**
         * Called back by the service when it has finished processing notifications,
         * releasing the wake lock if the service is now stopping.
         */
        fun finishStartingService(service: SmsMonitorService) {
            if (BuildConfig.DEBUG) v("SmsMonitorService: finishStartingService()")
            service.stopSelf()
        }

        @Contract(pure = true)
        fun getmCurrentService(): SmsMonitorService? {
            return mCurrentService
        }

        fun setmCurrentService(mCurrentService: SmsMonitorService?) {
            Companion.mCurrentService = mCurrentService
        }
    }
}