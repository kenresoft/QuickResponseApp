package com.kixfobby.security.quickresponse.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.*
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ActivityNearbyBinding
import com.kixfobby.security.quickresponse.home.Nearby
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.DatabaseAccess.Companion.getInstance
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.util.ViewAnimation

class Fire : BaseActivity() {
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
    private lateinit var binding: ActivityNearbyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNearbyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        init()
        binding.executePendingBindings()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.MainActivity_Billing_Const) {
            init()
            //Toast.makeText(this, String.valueOf(Constants.subscribe), Toast.LENGTH_SHORT).show();
        }
    }

    private fun init() {
        initComponent()
        supportActionBar!!.setTitle(R.string.fire_alert)
        val cty = PreferenceManager.getDefaultSharedPreferences(this).getString("ct", "cty")!!
            .trim { it <= ' ' }
        val databaseAccess = getInstance(this)
        code = databaseAccess!!.getFireCode(cty)
        if (code == null) {
            code = "911"
        }
        (findViewById<View>(R.id.tvStatus) as TextView).text = cty
        //Toast.makeText(this, cty + code, Toast.LENGTH_SHORT).show();
        binding.fabs.fabMedia.visibility = View.GONE

        ViewAnimation.initShowOut(binding.fabs.lytDashboard)
        ViewAnimation.initShowOut(binding.fabs.lytAccount)
        binding.backDrop.visibility = View.GONE
        binding.tvMap.visibility = View.GONE
        binding.fabs.fabFire.setImageResource(R.drawable.baseline_person_24)
        binding.fabs.fabNearby.setImageResource(R.drawable.baseline_location_on_24)

        binding.fabs.fabHome.setOnClickListener { v -> toggleFabMode(v) }
        binding.backDrop.setOnClickListener(View.OnClickListener { toggleFabMode(binding.fabs.fabHome) })

        binding.fabs.fabDashboard.setOnClickListener {
            startActivity(Intent(baseContext, Dashboard::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabAccount.setOnClickListener {
            startActivity(Intent(baseContext, ManageAccount::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabFire.setOnClickListener {
            startActivity(Intent(baseContext, Self::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabNearby.setOnClickListener {
            startActivity(Intent(baseContext, Nearby::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabTravel.setOnClickListener {
            startActivity(Intent(baseContext, Travel::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
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
        db!!.updatePage(Constants.FIRE)

        //val btnPanic = findViewById<Button>(R.id.btn_panic)
        binding.btnPanic.setOnClickListener { v -> onDial(v) }

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

                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            1 -> {
                //updateLocationUI()
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
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finishAffinity()
            }
            .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    private fun onDial(view: View?) {
        val i = Intent(Intent.ACTION_CALL)
        number = code!!.toInt()
        i.data = Uri.parse("tel:$number")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
        } else startActivity(i)
    }

    fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }
}