package com.kixfobby.security.quickresponse.home

//import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest
import android.app.Activity
import android.content.Context
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ActivityNearbyBinding
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.DatabaseAccess.Companion.getInstance
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.ui.*
import com.kixfobby.security.quickresponse.util.ViewAnimation
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class Nearby : BaseActivity() {
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var code: String? = null
    var number = 0
    private var rotate = false
    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private var mAuth: FirebaseAuth? = null
    private var user1: FirebaseUser? = null
    private var verified: String = "false"
    private lateinit var binding: ActivityNearbyBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser

        verified = Pref(this).get("isLoggedIn", false).toString()

        if (user1 == null || verified == "false") {
            var intent = Intent(this, RegisterAccount::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        } else {
            binding = ActivityNearbyBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)
            init()
            initComponent()
            supportActionBar!!.setTitle(R.string.nearby_alert)
            val cty = PreferenceManager.getDefaultSharedPreferences(this).getString("ct", "cty")!!
                .trim { it <= ' ' }
            val lanKey = PreferenceManager.getDefaultSharedPreferences(this).getString("language", "lan")
            val databaseAccess = getInstance(this)
            code = databaseAccess!!.getPoliceCode(cty)
            if (code == null) {
                code = "112"
            }
            binding.executePendingBindings()
            isPremiumPurchaseChecker()
        }
    }

    private fun isPremiumPurchaseChecker() {
        var uCheck = Pref(this).get("checkPaidUser", false)
        if (uCheck == true) {
            //BillingPremiumDialog.dismiss(this@Self)
            binding.btnPanic.setOnLongClickListener {
                //captureBackground()
                //binding.btnPanic.startAnimation(animation)

                if (ContextCompat.checkSelfPermission(
                        this@Nearby,
                        Manifest.permission.SEND_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    grantPermission(this, arrayOf(Manifest.permission.SEND_SMS))
                } else {
                    onDial()
                }
                false
            }
        }
    }

    fun onDial() {
        val i = Intent(Intent.ACTION_CALL)
        number = code!!.toInt()
        i.data = Uri.parse("tel:$number")
        if (ActivityCompat.checkSelfPermission(
                getActivity(this@Nearby)!!,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                getActivity(this@Nearby)!!,
                arrayOf(Manifest.permission.CALL_PHONE),
                1
            )
        } else startActivity(i)
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
        db!!.updatePage(Constants.NEARBY)

        binding.fabs.fabMedia.visibility = View.GONE

        MobileAds.initialize(this) {}

        adView = AdView(this)
        binding.adViewContainer.addView(adView)
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        binding.adViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
            if (!initialLayoutComplete) {
                initialLayoutComplete = true
                loadBanner()
            }
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                //Toast.makeText(baseContext, "Load Successful!", Toast.LENGTH_SHORT)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdOpened() {
                //Toast.makeText(baseContext, "Open Successful!", Toast.LENGTH_SHORT)
            }

            override fun onAdClicked() {
                //Toast.makeText(baseContext, "LClick Successful!", Toast.LENGTH_SHORT)
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

    private fun init() {
        val nearbyVM = ViewModelProvider(this).get(NearbyVM::class.java)
        //val billingVM = ViewModelProvider(this).get(BillingVM::class.java)
        binding.presenter = nearbyVM
        this.lifecycle.addObserver(nearbyVM)
        //this.lifecycle.addObserver(billingVM)
        nearbyVM.isPremiumPurchased.observe(this, isPremiumPurchasedObserver)

        ViewAnimation.initShowOut(binding.fabs.lytDashboard)
        ViewAnimation.initShowOut(binding.fabs.lytAccount)
        binding.backDrop.visibility = View.GONE
        binding.tvMap.visibility = View.GONE
        binding.fabs.fabFire.setImageResource(R.drawable.baseline_person_24)
        binding.fabs.fabNearby.setImageResource(R.drawable.baseline_local_fire_department_24)

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
            startActivity(Intent(baseContext, Fire::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabTravel.setOnClickListener {
            startActivity(Intent(baseContext, Travel::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
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

    private val isPremiumPurchasedObserver = Observer { aBoolean: Boolean? ->
        if (aBoolean != null) {
            // Dismisses BillingPremiumDialog after successful purchase of Premium Feature.
            //binding.executePendingBindings()
        }
    }


    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)
        builder.setMessage("Do you want to exit?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finishAffinity()
            }
            .setNegativeButton("No") { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    companion object {
        private val activity: Activity? = null
        private val context: Context? = null
    }

    private fun Any.observe(self: Self, premiumPurchasedObserver: Observer<Boolean>) {

    }

}