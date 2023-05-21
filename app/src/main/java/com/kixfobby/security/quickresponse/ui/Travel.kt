package com.kixfobby.security.quickresponse.ui

import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.camera.view.CameraController
import androidx.core.content.ContextCompat
import com.androidhiddencamera.HiddenCameraFragment
import com.google.android.gms.ads.*
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.alarm.NotificationScheduler
import com.kixfobby.security.quickresponse.databinding.ActivityTravelBinding
import com.kixfobby.security.quickresponse.home.Nearby
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.receiver.AlarmReceiver
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.util.ViewAnimation
import com.kixfobby.security.quickresponse.widget.AlarmTimer
import java.text.SimpleDateFormat
import java.util.*


class Travel : BaseActivity(), AlarmTimer.OnCountDownListener {
    private var alarmEndTimeValue: Int? = null
    var number = 0
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var code: String? = null
    private var rotate = false
    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private lateinit var binding: ActivityTravelBinding
    private var mHiddenCameraFragment: HiddenCameraFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravelBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.MainActivity_Billing_Const -> {
                init()
                //Toast.makeText(this, String.valueOf(Constants.subscribe), Toast.LENGTH_SHORT).show();
            }
            CameraController.VIDEO_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    val imageUri = data!!.data
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                    Toast.makeText(this, "Video saved to: $imageUri", Toast.LENGTH_LONG).show()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun init() {
        initComponent()
        supportActionBar!!.title = getString(R.string.travel_alert)

        binding.fabs.fabMedia.visibility = View.GONE
        binding.tvStatus.visibility = View.GONE

        ViewAnimation.initShowOut(binding.fabs.lytDashboard)
        ViewAnimation.initShowOut(binding.fabs.lytAccount)
        binding.backDrop.visibility = View.GONE
        binding.tvMap.visibility = View.GONE
        binding.fabs.fabFire.setImageResource(R.drawable.baseline_person_24)
        binding.fabs.fabNearby.setImageResource(R.drawable.baseline_location_on_24)
        binding.fabs.fabTravel.setImageResource(R.drawable.baseline_local_fire_department_24)

        binding.fabs.fabHome.setOnClickListener { v -> toggleFabMode(v) }
        binding.backDrop.setOnClickListener(View.OnClickListener { toggleFabMode(binding.fabs.fabHome) })

        binding.fabs.fabDashboard.setOnClickListener {
            startActivity(Intent(baseContext, Dashboard::class.java))
            animate()
        }
        binding.fabs.fabAccount.setOnClickListener {
            startActivity(Intent(baseContext, ManageAccount::class.java))
            animate()
        }
        binding.fabs.fabFire.setOnClickListener {
            startActivity(Intent(baseContext, Self::class.java))
            animate()
        }
        binding.fabs.fabNearby.setOnClickListener {
            startActivity(Intent(baseContext, Nearby::class.java))
            animate()
        }
        binding.fabs.fabTravel.setOnClickListener {
            startActivity(Intent(baseContext, Fire::class.java))
            animate()
        }

        binding.tvStatus.setOnClickListener { openCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        adView.destroy()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
        grantPermissions(this)
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onStart() {
        super.onStart()
        //captureBackground()
    }

    override fun onStop() {
        super.onStop()

    }

    private fun initComponent() {
        db = PageDatabase(this)
        c = db!!.allPages
        row = c!!.moveToLast()
        if (row) {
            id = c!!.getString(0)
            page = c!!.getString(1)
        }
        db!!.updatePage(Constants.ALARM)

        binding.btnPanic.setOnLongClickListener { v ->
            onAlarm(v)

        }

        MobileAds.initialize(this) {}

        adView = AdView(this)
        binding.adViewContainer.addView(adView)
        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner()
            }
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Toast.makeText(baseContext, "Load Successful!", Toast.LENGTH_SHORT)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdOpened() {
                Toast.makeText(baseContext, "Open Successful!", Toast.LENGTH_SHORT)
            }

            override fun onAdClicked() {
                Toast.makeText(baseContext, "LClick Successful!", Toast.LENGTH_SHORT)
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }
    }

    private val adSize: AdSize
        get() {
            val display = windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        }

    private fun loadBanner() {
        adView.adUnitId = Self.AD_UNIT_ID
        adView.adSize = adSize
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun toggleFabMode(v: View) {
        rotate = ViewAnimation.rotateFab(v, !rotate)
        if (rotate) {
            ViewAnimation.showIn(binding.fabs.lytDashboard)
            ViewAnimation.showIn(binding.fabs.lytAccount)
            binding.backDrop.setVisibility(View.VISIBLE)
        } else {
            ViewAnimation.showOut(binding.fabs.lytDashboard)
            ViewAnimation.showOut(binding.fabs.lytAccount)
            binding.backDrop.setVisibility(View.GONE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "Help").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_help_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 1, Menu.NONE, "Refresh Location").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_refresh_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                return true
            }
            1 -> {
                /*startActivity(Intent(this@Travel, MainActivity::class.java))
                animate()*/
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setMessage("Do you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                animate()
                finishAffinity()
            }
            .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun onAlarm(view: View?): Boolean {
        showCustomDialog()

        //countDownTimer.start()
        //startBtn.isEnabled = false
        //Toast.makeText(baseContext, "aaa", Toast.LENGTH_SHORT).show()
        return true
    }

    fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private fun showCustomDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
        dialog.setContentView(R.layout.set_alarm)
        dialog.setCancelable(true)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        (dialog.findViewById<View>(R.id.time_picker) as TimePicker).setOnTimeChangedListener { timePicker: TimePicker, i: Int, i1: Int ->
            val h = SimpleDateFormat("HH")
            val hour = h.format(Date()).toInt()
            val m = SimpleDateFormat("mm")
            val minute = m.format(Date()).toInt()

            val currentT = (hour * 60 + minute)
            val nextT = (i * 60 + i1)
            val interval = currentT - nextT
            if (nextT < currentT) {
                alarmEndTimeValue = 1440 - interval
                val alarmEndTime = minToMil(alarmEndTimeValue!!)
                Toast.makeText(applicationContext, alarmEndTimeValue.toString(), Toast.LENGTH_SHORT).show()
                Pref(this).put("alarmEndTime", alarmEndTime)
                //NotificationScheduler.setReminder(this, AlarmReceiver::class.java, false, 10000)
            } else {
                alarmEndTimeValue = nextT - currentT
                val alarmEndTime = minToMil(alarmEndTimeValue!!)
                Toast.makeText(applicationContext, alarmEndTimeValue.toString(), Toast.LENGTH_SHORT).show()
                Pref(this).put("alarmEndTime", alarmEndTime)

            }

        }
        //val et_post = dialog.findViewById<View>(R.id.et_post) as EditText

        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener { dialog.dismiss() }
        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {
            dialog.dismiss()

            val alarmEndTime = minToMil(alarmEndTimeValue!!)

            val duration = minToMil(60)
            val endTime = System.currentTimeMillis() + alarmEndTime
            val startTime = System.currentTimeMillis() + duration

            Pref(baseContext).put("duration", duration)
            Pref(baseContext).put("endTime", endTime)
            Pref(baseContext).put("startTime", startTime)
            Pref(baseContext).put("currentTimeMillis", System.currentTimeMillis())

            NotificationScheduler.setReminder(baseContext, AlarmReceiver::class.java, false, duration)
            Toast.makeText(applicationContext, "Alarm saved!", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    override fun onCountDownActive(time: String) {
        runOnUiThread {
            binding.tvStatus.text = time
            //toast(this, "Seconds = " + countDownTimer.getSecondsTillCountDown() + " Minutes=" + countDownTimer.getMinutesTillCountDown())
        }
    }

    override fun onCountDownFinished() {
        runOnUiThread {
            toast(this, "Finished")
            //startBtn.isEnabled = true
            //resumeBtn.isEnabled = false
        }
    }


    companion object {
        private val VIDEO_CAPTURE = 101

    }
}