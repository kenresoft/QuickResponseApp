package com.kixfobby.security.quickresponse

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.crash.config.CaocConfig
import com.kixfobby.security.localization.ui.LocalizationApplication
import com.kixfobby.security.quickresponse.BaseActivity.Companion.printLog
import com.kixfobby.security.quickresponse.service.UpdateService
import com.kixfobby.security.quickresponse.storage.Actions
import com.kixfobby.security.quickresponse.storage.ServiceState
import com.kixfobby.security.quickresponse.storage.getServiceState
import dagger.hilt.android.HiltAndroidApp
import top.defaults.view.TextButton
import top.defaults.view.TextButtonEffect
import java.util.*

const val TAG = "BaseApplication"

@HiltAndroidApp
public open class BaseApplication : LocalizationApplication() {
    private var lastConfigChange: Long = 0

    override fun getDefaultLanguage(): Locale {
        return Locale.ENGLISH
    }

    override fun onCreate() {
        /*if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            );
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build()
            );
        }*/
        super.onCreate();
        printLog(TAG, "onCreate:");

        FirebaseApp.getApps(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val defaults = TextButton.Defaults.get()
        defaults[R.styleable.TextButton_backgroundEffect] = TextButtonEffect.BACKGROUND_EFFECT_RIPPLE
        defaults[R.styleable.TextButton_rippleColor] = -0x10000
        lastConfigChange = Date().time

        //log("START THE FOREGROUND SERVICE ON DEMAND")
        actionOnService(Actions.START)

        /*log("STOP THE FOREGROUND SERVICE ON DEMAND")
        actionOnService(Actions.STOP)*/

        CaocConfig.Builder.create().apply()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    private fun actionOnService(action: Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return
        Intent(this, UpdateService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                printLog(TAG, "Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            printLog(TAG, "Starting the service in < 26 Mode")
            startService(it)
        }
    }

    fun wasLastConfigChangeRecent(buffer: Int): Boolean {
        return Date().time - lastConfigChange <= buffer
    }

    fun checkService(serviceClass: Class<*>?) {
        val intent = Intent(applicationContext, serviceClass)
        if (PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_NO_CREATE) == null) {
            startService(intent)
        } else {
            // .makeText(getApplicationContext(), "service is already running!", Toast.LENGTH_SHORT).show();
        }
    }

/*private class CustomEventListener : CustomActivityOnCrash.EventListener {
    override fun onLaunchErrorActivity() {
        Log.i(TAG, "onLaunchErrorActivity()")
    }

    override fun onRestartAppFromErrorActivity() {
        Log.i(
            TAG,
            "onRestartAppFromErrorActivity()"
        )
    }

    override fun onCloseAppFromErrorActivity() {
        Log.i(TAG, "onCloseAppFromErrorActivity()")
    }
}*/
}