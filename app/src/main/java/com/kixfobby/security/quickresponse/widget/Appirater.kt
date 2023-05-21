package com.kixfobby.security.quickresponse.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kixfobby.security.quickresponse.R

object Appirater {
    private const val PREF_LAUNCH_COUNT = "launch_count"
    private const val PREF_EVENT_COUNT = "event_count"
    private const val PREF_RATE_CLICKED = "rateclicked"
    private const val PREF_DONT_SHOW = "dontshow"
    private const val PREF_DATE_REMINDER_PRESSED = "date_reminder_pressed"
    private const val PREF_DATE_FIRST_LAUNCHED = "date_firstlaunch"
    private const val PREF_APP_VERSION_CODE = "versioncode"
    private const val PREF_APP_LOVE_CLICKED = "loveclicked"

    fun appLaunched(mContext: Context) {
        val testMode = mContext.resources.getBoolean(R.bool.appirator_test_mode)
        val prefs = mContext.getSharedPreferences(mContext.packageName + ".appirater", 0)
        if (!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {
            return
        }
        val editor = prefs.edit()
        if (testMode) {
            if (prefs.getBoolean(PREF_APP_LOVE_CLICKED, false)) {
                showRateDialog(mContext, editor)
            } else {
                //showLoveDialog(mContext, editor)
            }
            return
        }

        // Increment launch counter
        var launch_count = prefs.getLong(PREF_LAUNCH_COUNT, 0)

        // Get events counter
        var event_count = prefs.getLong(PREF_EVENT_COUNT, 0)

        // Get date of first launch
        var date_firstLaunch = prefs.getLong(PREF_DATE_FIRST_LAUNCHED, 0)

        // Get reminder date pressed
        val date_reminder_pressed = prefs.getLong(PREF_DATE_REMINDER_PRESSED, 0)
        try {
            val appVersionCode = mContext.packageManager.getPackageInfo(mContext.packageName, 0).versionCode
            if (prefs.getInt(PREF_APP_VERSION_CODE, 0) != appVersionCode) {
                //Reset the launch and event counters to help assure users are rating based on the latest version. 
                launch_count = 0
                event_count = 0
                editor.putLong(PREF_EVENT_COUNT, event_count)
            }
            editor.putInt(PREF_APP_VERSION_CODE, appVersionCode)
        } catch (e: Exception) {
            //do nothing
        }
        launch_count++
        editor.putLong(PREF_LAUNCH_COUNT, launch_count)
        if (date_firstLaunch == 0L) {
            date_firstLaunch = System.currentTimeMillis()
            editor.putLong(PREF_DATE_FIRST_LAUNCHED, date_firstLaunch)
        }

        // Wait at least n days or m events before opening
        if (launch_count >= mContext.resources.getInteger(R.integer.appirator_launches_until_prompt)) {
            val millisecondsToWait =
                mContext.resources.getInteger(R.integer.appirator_days_until_prompt) * 24 * 60 * 60 * 1000L
            if (System.currentTimeMillis() >= date_firstLaunch + millisecondsToWait || event_count >= mContext.resources.getInteger(
                    R.integer.appirator_events_until_prompt
                )
            ) {
                if (date_reminder_pressed == 0L) {
                    if (prefs.getBoolean(PREF_APP_LOVE_CLICKED, false)) {
                        showRateDialog(mContext, editor)
                    } else {
                        //showLoveDialog(mContext, editor)
                    }
                } else {
                    val remindMillisecondsToWait =
                        mContext.resources.getInteger(R.integer.appirator_days_before_reminding) * 24 * 60 * 60 * 1000L
                    if (System.currentTimeMillis() >= remindMillisecondsToWait + date_reminder_pressed) {
                        if (prefs.getBoolean(PREF_APP_LOVE_CLICKED, false)) {
                            showRateDialog(mContext, editor)
                        } else {
                            //showLoveDialog(mContext, editor)
                        }
                    }
                }
            }
        }
        editor.commit()
    }

    fun rateApp(mContext: Context) {
        val prefs = mContext.getSharedPreferences(mContext.packageName + ".appirater", 0)
        val editor = prefs.edit()
        rateApp(mContext, editor)
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    fun significantEvent(mContext: Context) {
        val testMode = mContext.resources.getBoolean(R.bool.appirator_test_mode)
        val prefs = mContext.getSharedPreferences(mContext.packageName + ".appirater", 0)
        if (!testMode && (prefs.getBoolean(PREF_DONT_SHOW, false) || prefs.getBoolean(PREF_RATE_CLICKED, false))) {
            return
        }
        var event_count = prefs.getLong(PREF_EVENT_COUNT, 0)
        event_count++
        prefs.edit().putLong(PREF_EVENT_COUNT, event_count).apply()
    }

    private fun rateApp(mContext: Context, editor: SharedPreferences.Editor?) {
        mContext.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(String.format(mContext.getString(R.string.appirator_market_url), mContext.packageName))
            )
        )
        if (editor != null) {
            editor.putBoolean(PREF_RATE_CLICKED, true)
            editor.commit()
        }
    }

    @SuppressLint("NewApi")
    public fun showRateDialog(mContext: Context, editor: SharedPreferences.Editor?) {
        val appName = mContext.getString(R.string.appirator_app_title)
        val dialog = Dialog(mContext)
        if (Build.VERSION.RELEASE.startsWith("1.") || Build.VERSION.RELEASE.startsWith("2.0") || Build.VERSION.RELEASE.startsWith(
                "2.1"
            )
        ) {
            //No dialog title on pre-froyo devices
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        } else if (mContext.resources.displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW || mContext.resources.displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
            val display = (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            val rotation = display.rotation
            if (rotation == 90 || rotation == 270) {
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            } else {
                dialog.setTitle(String.format(mContext.getString(R.string.rate_title), appName))
            }
        } else {
            dialog.setTitle(String.format(mContext.getString(R.string.rate_title), appName))
        }
        val layout = LayoutInflater.from(mContext).inflate(R.layout.appirater, null) as LinearLayout
        val tv = layout.findViewById<View>(R.id.message) as TextView
        tv.text = String.format(mContext.getString(R.string.rate_message), appName)
        val rateButton = layout.findViewById<View>(R.id.rate) as Button
        rateButton.text = String.format(mContext.getString(R.string.rate), appName)
        rateButton.setOnClickListener {
            rateApp(mContext, editor)
            dialog.dismiss()
        }
        val rateLaterButton = layout.findViewById<View>(R.id.rateLater) as Button
        rateLaterButton.text = mContext.getString(R.string.rate_later)
        rateLaterButton.setOnClickListener {
            if (editor != null) {
                editor.putLong(PREF_DATE_REMINDER_PRESSED, System.currentTimeMillis())
                editor.commit()
            }
            dialog.dismiss()
        }
        val cancelButton = layout.findViewById<View>(R.id.cancel) as Button
        cancelButton.text = mContext.getString(R.string.rate_cancel)
        cancelButton.setOnClickListener {
            if (editor != null) {
                editor.putBoolean(PREF_DONT_SHOW, true)
                editor.commit()
            }
            dialog.dismiss()
        }
        dialog.setContentView(layout)
        dialog.show()
    }

    /*private fun showLoveDialog(mContext: Context, editor: SharedPreferences.Editor?) {
        val dialog = Dialog(mContext)
        val layout = LayoutInflater.from(mContext).inflate(R.layout.loveapp, null) as LinearLayout
        val textView = layout.findViewById<View>(R.id.love_dialog_message) as TextView
        val yesButton = layout.findViewById<View>(R.id.love_dialog_yes) as Button
        val noButton = layout.findViewById<View>(R.id.love_dialog_no) as Button
        textView.text = String.format(mContext.getString(R.string.love_dialog_content))
        yesButton.text = String.format(mContext.getString(R.string.love_dialog_yes))
        noButton.text = String.format(mContext.getString(R.string.love_dialog_no))
        yesButton.setOnClickListener {
            if (editor != null) {
                editor.putBoolean(PREF_APP_LOVE_CLICKED, true)
                editor.commit()
            }
            dialog.dismiss()
            showRateDialog(mContext, editor)
        }
        noButton.setOnClickListener { v ->
            if (editor != null) {
                editor.putBoolean(PREF_DONT_SHOW, true)
                editor.commit()
            }
            dialog.dismiss()
            val intent = Intent()
            intent.action = "com.sbstrm.appirater.Appirater"
            intent.putExtra("HATE_APP", true)
            v.context.sendBroadcast(intent)
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layout, dialog.window!!.attributes)
        dialog.show()
    }*/
}