package com.kixfobby.security.quickresponse.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.CameraController
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.androidhiddencamera.HiddenCameraFragment
import com.google.android.gms.ads.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.ProcessMainClass
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ActivitySelfBinding
import com.kixfobby.security.quickresponse.helper.ClipBoardManager
import com.kixfobby.security.quickresponse.helper.network.NetworkManager
import com.kixfobby.security.quickresponse.model.ChatBase
import com.kixfobby.security.quickresponse.model.LocationBase
import com.kixfobby.security.quickresponse.model.Message
import com.kixfobby.security.quickresponse.model.SmsBase
import com.kixfobby.security.quickresponse.receiver.RestartServiceBroadcastReceiver
import com.kixfobby.security.quickresponse.service.MyUploadService
import com.kixfobby.security.quickresponse.storage.*
import com.kixfobby.security.quickresponse.ui.*
import com.kixfobby.security.quickresponse.util.ViewAnimation
import com.kixfobby.security.quickresponse.util.ViewAnimation.collapse
import com.kixfobby.security.quickresponse.util.ViewAnimation.expand
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.apply
import kotlin.arrayOf
import kotlin.let
import kotlin.run
import kotlin.toString
import kotlin.with


@AndroidEntryPoint
class Self : BaseActivity(), NetworkManager.NetworkStateCallback {
    private lateinit var broadcastReceiver: BroadcastReceiver
    private var fileUri: Uri? = null
    private var powerMenu: PowerMenu? = null
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var animation: Animation? = null
    private var receiver: NetworkChangeReceiver? = null
    private var filter: IntentFilter? = null
    private var isConnected = false
    private var locationAddress: Address? = null
    private var mLocation: String? = null
    private var mAddress: String? = null
    private var mTime: String? = null
    private var mLastUpdateTime: String? = null
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var mLocationCallback: LocationCallback? = null
    private var mCurrentLocation: Location? = null
    private var mRequestingLocationUpdates: Boolean? = null
    private var chatReference: DatabaseReference? = null
    private var userReference: DatabaseReference? = null
    private var me: DatabaseReference? = null
    private var you: DatabaseReference? = null
    private var myFriends: DatabaseReference? = null
    private var yourFriends: DatabaseReference? = null
    private val myStatus: DatabaseReference? = null
    private var mFriend: DatabaseReference? = null
    private var yFriend: DatabaseReference? = null
    private var myMessageDate: DatabaseReference? = null
    private var yourMessageDate: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var user1: FirebaseUser? = null
    private var rotate = false
    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private lateinit var binding: ActivitySelfBinding
    private var verified: String = "false"
    private var mHiddenCameraFragment: HiddenCameraFragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding = ActivitySelfBinding.inflate(layoutInflater)
        //val view = binding.root
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser

        verified = Pref(this).get("isLoggedIn", false).toString()

        if (user1 != null && verified != "false") {
            //setContentView(view)
            binding = DataBindingUtil.setContentView(this, R.layout.activity_self);
            //Toast.makeText(this, Pref(getApplication()).get("bill", "...").toString(), Toast.LENGTH_SHORT).show()
            initUi()
            initComponent()
            initLocation()
            restoreValuesFromBundle(savedInstanceState)
            savedInstanceState?.let { fileUri = it.getParcelable(KEY_FILE_URI) }
            onNewIntent(intent)
            binding.executePendingBindings()
            isPremiumPurchaseChecker()   /* I used this instead of the actual "isPremiumPurchasedObserver" which works with LiveData<Boolean> */
        } else {
            var intent = Intent(this, RegisterAccount::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (user1 != null && verified != "false") {
            adView.destroy()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                RestartServiceBroadcastReceiver.scheduleJob(applicationContext)
            } else {
                val bck = ProcessMainClass()
                bck.launchService(applicationContext)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (user1 != null && verified != "false") {
            adView.resume()
            filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            receiver = NetworkChangeReceiver()
            registerReceiver(receiver, filter)
            grantPermissions(this)
            startLocationUpdates()

            GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    launch(Dispatchers.IO) {
                        val handler = Handler(mainLooper)
                        handler.postDelayed(Runnable { updateLocationUI() }, 1000L)
                    }
                    delay(4000L)
                }
            }
            binding.btnPanic.alpha = 0f
            binding.btnPanic.animate().alpha(1f).duration = 1000
            if (mRequestingLocationUpdates!! && checkPermissions()) {
            }
            checkPaidUser()
        }
    }

    override fun onPause() {
        super.onPause()
        if (user1 != null && verified != "false") {
            adView.pause()
            unregisterReceiver(receiver)
            stopLocationUpdates()
        }
    }

    override fun onStart() {
        super.onStart()
        if (user1 != null && verified != "false") {
            val manager = LocalBroadcastManager.getInstance(this)
            manager.registerReceiver(broadcastReceiver, MyUploadService.intentFilter)
            captureBackground()
        }
    }

    override fun onStop() {
        super.onStop()
        if (user1 != null && verified != "false") {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.let {
            super.onSaveInstanceState(it)
            if (user1 == null || verified == "false") return
            it.putBoolean("is_requesting_updates", mRequestingLocationUpdates!!)
            it.putParcelable("last_known_location", mCurrentLocation)
            it.putString("last_updated_on", mLastUpdateTime)
            it.putParcelable(KEY_FILE_URI, fileUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult:$requestCode:$resultCode:$data")
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                RESULT_OK -> Log.e(TAG, "User agreed to make required location settings changes.")
                RESULT_CANCELED -> {
                    Log.e(TAG, "User chose not to make required location settings changes.")
                    mRequestingLocationUpdates = false
                }
            }

            CameraController.VIDEO_CAPTURE -> {
                updateLocationUI()
                when (resultCode) {
                    RESULT_OK -> {
                        Pref(this).put("fileFormat", "video")
                        fileUri = data?.data!!
                        uploadFromUri(this, fileUri!!)
                        Toast.makeText(this, "Video saved to: $fileUri", Toast.LENGTH_LONG).show()
                    }
                    RESULT_CANCELED -> {
                        Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show()
                    }
                }
            }

            CameraController.IMAGE_CAPTURE -> {
                updateLocationUI()
                Pref(this).put("fileFormat", "image")
                when (resultCode) {
                    RESULT_OK -> {
                        var imgUri = Uri.fromFile(File(Pref(baseContext).get("imageFile", "imageFile").toString()))
                        uploadFromUri(this, imgUri!!)
                        Toast.makeText(this, "Picture saved to: $fileUri", Toast.LENGTH_LONG).show()
                    }
                    RESULT_CANCELED -> {
                        Toast.makeText(this, "Picture capture cancelled.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "Failed to capture pictue", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onNetworkLost() {
        Toast.makeText(this, "NETWORK LOST!", Toast.LENGTH_SHORT).show()
    }

    public fun uploadFromUri(c: Context, uploadUri: Uri) {
        Log.d(TAG, "uploadFromUri:src: $uploadUri")
        fileUri = uploadUri
        getActivity(c)?.startService(
            Intent(this, MyUploadService::class.java)
                .putExtra(MyUploadService.EXTRA_FILE_URI, uploadUri)
                .setAction(MyUploadService.ACTION_UPLOAD)
        )
    }

    private fun onUploadResultIntent(intent: Intent) {
        fileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI)
    }

    private val isPremiumPurchasedObserver = Observer { aBoolean: Boolean? ->
        if (aBoolean != null) {
            // Dismisses BillingPremiumDialog after successful purchase of Premium Feature.
            //binding.executePendingBindings()
        }
    }

    private fun isPremiumPurchaseChecker() {
        var uCheck = Pref(this).get("checkPaidUser", false)
        if (uCheck == true) {
            //BillingPremiumDialog.dismiss(this@Self)
            binding.btnPanic.setOnLongClickListener {
                captureBackground()
                binding.btnPanic.startAnimation(animation)

                if (ContextCompat.checkSelfPermission(
                        this@Self,
                        Manifest.permission.SEND_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    grantPermission(this, arrayOf(Manifest.permission.SEND_SMS))
                } else {
                    // Permission has already been granted
                    var messageText = Pref(this@Self).get(Constants.SMS_MESSAGE, "No Message Available!")
                    val currentLocation = Pref(this@Self).get(Constants.CURRENT_LOCATION, false)
                    val customLocation = Pref(this@Self).get(Constants.CUSTOM_LOCATION, "null")
                    val country = Pref(this@Self).get("country", "country")
                    val state = Pref(this@Self).get("state", "state")
                    val zip = Pref(this@Self).get("zip", "zip")
                    if (messageText == "No Message Available!" || messageText == null) {
                        messageText = "I need help come ASAP!"
                    }

                    messageText += if (!currentLocation) {
                        "$mLocation"
                    } else if (customLocation != null) {
                        "$customLocation"
                    } else "\n$zip, $state, $country"

                    val smsMan = SmsManager.getDefault()
                    val recipients = ContactManager.getSavedPersons(applicationContext)
                    if (recipients.size != 0) {
                        for (p in recipients) {
                            smsMan.sendTextMessage(p.number, null, messageText, null, null)
                            val s = SmsBase(p.number, p.name!!, messageText, date)
                            SmsBaseManager.saveSms(s, baseContext)
                        }
                        val uid: Set<String>? = Pref(this@Self).get("contacts_uid_set", setOf("Contact Uid"))
                        val uidList: MutableList<String> = ArrayList()
                        if (uid != null) {
                            uidList.addAll(uid)
                        }
                        val name: Set<String>? = Pref(this@Self).get("contacts_name_set", setOf("Contact Name"))
                        val nameList: MutableList<String> = ArrayList()
                        if (name != null) {
                            nameList.addAll(name)
                        }
                        val phone: Set<String>? =
                            Pref(this@Self).get("contacts_phone_set", setOf("Contact Phone"))
                        val phoneList: MutableList<String> = ArrayList()
                        if (phone != null) {
                            phoneList.addAll(phone)
                        }
                        for (i in uidList.indices) {
                            mFriend = myFriends!!.child(uidList[i])
                            you = chatReference!!.child(uidList[i])
                            yourFriends = you!!.child("Friends")
                            yFriend = yourFriends!!.child(user1!!.uid)
                            myMessageDate = mFriend!!.child(date)
                            yourMessageDate = yFriend!!.child(date)
                            yourMessageDate!!.setValue(
                                Message()
                                    .setMessage(messageText) //.setUserPicture(user.getProfilePicture())
                                    .setSenderName(nameList[i]) //.setSenderUid(user.getUid())
                                    .setMessageStatus("Not seen")
                                    .setMessageType("Inbox")
                                    .setDate(date)
                            )
                            val s = ChatBase(phoneList[i], nameList[i], messageText, date)
                            ChatBaseManager.saveChat(s, baseContext)
                        }
                        showAlert(messageText)
                    } else {
                        toast(this, "No Security contact saved!")
                    }
                }
                false
            }
        }
    }

    private fun initComponent() {
        db = PageDatabase(this)
        c = db!!.allPages
        row = c!!.moveToLast()
        if (row) {
            id = c!!.getString(0)
            page = c!!.getString(1)
        }
        db!!.updatePage(Constants.SELF)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive:$intent")
            }
        }

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
                //toast(this@Self, "Load Successful!")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
            }

            override fun onAdOpened() {
                //toast(this@Self, "Open Successful!")
            }

            override fun onAdClicked() {
                //toast(this@Self, "AdClick Successful!")
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
        adView.adUnitId = AD_UNIT_ID
        adView.adSize = adSize
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }


    private fun initUi() {
        supportActionBar!!.setTitle(R.string.self_alert)
        mRequestingLocationUpdates = true
        binding.btnPanic.setAnimation(AnimationUtils.loadAnimation(this@Self, R.anim.blink))
        ad1 = Pref(this@Self).get("vadmin1", "adm1")
        ad2 = Pref(this@Self).get("vadmin2", "adm2")
        ad3 = Pref(this@Self).get("vadmin3", "adm3")
        ad4 = Pref(this@Self).get("vadmin4", "adm4")
        ad5 = Pref(this@Self).get("vadmin5", "adm5")
        ad6 = Pref(this@Self).get("vadmin6", "adm6")
        ad7 = Pref(this@Self).get("vadmin7", "adm7")
        ad8 = Pref(this@Self).get("vadmin8", "adm8")
        ad9 = Pref(this@Self).get("vadmin9", "adm9")
        ad10 = Pref(this@Self).get("vadmin10", "adm10")

        val selfVM = ViewModelProvider(this).get(SelfVM::class.java)
        //val billingVM = ViewModelProvider(this).get(BillingVM::class.java)
        binding.presenter = selfVM
        this.lifecycle.addObserver(selfVM)
        userReference = FirebaseDatabase.getInstance().getReference("User")
        chatReference = FirebaseDatabase.getInstance().getReference("Chat")
        me = chatReference!!.child(user1!!.getUid())
        myFriends = me!!.child("Friends")

        //myStatus = me.child("Status");
        selfVM.isPremiumPurchased.observe(this, isPremiumPurchasedObserver)

        animation = AnimationUtils.loadAnimation(this, R.anim.shake)
        ViewAnimation.initShowOut(binding.fabs.lytDashboard)
        ViewAnimation.initShowOut(binding.fabs.lytAccount)
        binding.backDrop.setVisibility(View.GONE)
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
        binding.fabs.dash.setOnClickListener {
            startActivity(Intent(baseContext, Dashboard::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.acct.setOnClickListener {
            startActivity(Intent(baseContext, ManageAccount::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        binding.fabs.fabFire.setOnClickListener {
            startActivity(Intent(baseContext, Fire::class.java))
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
        binding.fabs.fabMedia.setOnClickListener {
            powerMenu = PowerMenu.Builder(this)
                .addItem(PowerMenuItem("Image capture", false)) // add an item.
                .addItem(PowerMenuItem("Video capture", false)) // aad an item list.
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
                .setMenuRadius(12f) // sets the corner radius.
                .setMenuShadow(5f) // sets the shadow.
                .setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setTextGravity(Gravity.CENTER)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                .setOnMenuItemClickListener(object : OnMenuItemClickListener<PowerMenuItem?> {
                    override fun onItemClick(position: Int, item: PowerMenuItem?) {
                        when (position) {
                            0 -> {
                                openCamera()
                            }
                            1 -> {
                                openVideo()
                            }
                        }
                        powerMenu?.setSelectedPosition(position) // change selected item
                        powerMenu?.dismiss()
                    }
                })
                .build()
            powerMenu?.showAsDropDown(binding.fabs.fabMedia);
        }

        with(binding.tvMap) { setOnClickListener(View.OnClickListener { showMap() }) }
        binding.cancel.setOnClickListener { collapse(binding.banner) }
    }


    private fun initLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                // location is received
                mCurrentLocation = locationResult.lastLocation
                mLastUpdateTime = date //DateFormat.getTimeInstance().format(new Date());
            }
        }
        mRequestingLocationUpdates = false
        mLocationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL_IN_MILLISECONDS
            fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(this)
            mLocationSettingsRequest = builder.build()
        }
    }

    /**
     * Restoring values from saved instance state
     */
    private fun restoreValuesFromBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates")
            }
            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location")
            }
            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on")
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (mCurrentLocation != null) {
            mCurrentLocation?.run {
                val geocoder: Geocoder
                val addresses: List<Address>
                geocoder = Geocoder(baseContext, Locale.getDefault())
                try {
                    addresses = geocoder.getFromLocation(
                        latitude,
                        longitude,
                        1
                    ) // Here 1 represent max location result to be returned, by documents it recommended 1 to 5
                    locationAddress = addresses[0]
                    address
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                address
                mLocation = "Latitude: $latitude, Longitude: $longitude; $mAddress"
                mTime = mLastUpdateTime
                val l = mTime?.let { LocationBase(mLocation!!, it) }
                l?.let { LocationBaseManager.saveLocation(baseContext, it) }
                binding.tvMap.visibleBadge(true)
                showLastKnownLocation()
            }
        }
        binding.tvMap.visibleBadge(false)
        showLastKnownLocation()
    }

    val address: Unit
        get() {
            val mstreet: String
            if (locationAddress != null) {
                locationAddress?.let {
                    val street = it.getAddressLine(0)
                    val locality = it.locality
                    val lga = it.subAdminArea
                    val state = it.adminArea
                    val country = it.countryName

                    with(street) {
                        mstreet = if (!TextUtils.isEmpty(locality) && contains(locality)) {
                            val int1 = lastIndexOf(",")
                            val str1 = substring(0, int1)
                            val int2 = lastIndexOf(",")
                            str1.substring(0, int2)
                        } else if (!TextUtils.isEmpty(country) && contains(country)) {
                            val int1 = lastIndexOf(",")
                            substring(0, int1)
                        } else this

                    }

                    var currentLocation: String
                    if (!TextUtils.isEmpty(street)) {
                        currentLocation = if (mstreet != "Unnamed Road") "$mstreet,\n" else ""
                        if (!TextUtils.isEmpty(locality)) currentLocation += "(locality: $locality),\n"
                        if (!TextUtils.isEmpty(lga)) currentLocation += "$lga, "
                        if (!TextUtils.isEmpty(state)) currentLocation += "$state, "
                        if (!TextUtils.isEmpty(country)) currentLocation += "$country."
                        mAddress = currentLocation
                    }
                }

            } else
                mAddress = "#unspecified address!"
        }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */

///// HANGING HERE
    private fun startLocationUpdates() {
        mSettingsClient
            ?.checkLocationSettings(mLocationSettingsRequest!!)
            ?.addOnSuccessListener(this) {
                Log.i(TAG, "All location settings are satisfied.")
                mFusedLocationClient!!.requestLocationUpdates(
                    mLocationRequest!!,
                    mLocationCallback!!,
                    Looper.myLooper()!!
                )
            }
            ?.addOnFailureListener(this) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        Log.i(
                            TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings "
                        )
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(this@Self, REQUEST_CHECK_SETTINGS)
                        } catch (sie: SendIntentException) {
                            Log.i(TAG, "PendingIntent unable to execute request.")
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        val errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings."
                        Log.e(TAG, errorMessage)
                        toast(this@Self, errorMessage)
                    }
                }
            }
    }

    fun stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
            ?.removeLocationUpdates(mLocationCallback!!)
            ?.addOnCompleteListener(this) { }
    }

    @SuppressLint("SetTextI18n")
    fun showLastKnownLocation() {
        with(binding.tvStatus) {
            if (mCurrentLocation != null) {
                alpha = 0f
                animate().alpha(1f).duration = 1100
                text = "$mLocation On $mTime"
                setOnClickListener {
                    ClipBoardManager().copyToClipboard(this@Self, text as String)
                    toast(this@Self, "Location copied to clipboard!")
                }
            } else {
                alpha = 0f
                animate().alpha(1f).duration = 1
                text = "Last known location is not available!"
                setOnClickListener {
                    updateLocationUI()
                }
            }
        }
    }

    fun showMap() {
        mCurrentLocation?.let {
            if (mCurrentLocation != null) {
                val imap = Intent(this@Self, MapsActivity::class.java)
                val bundle = Bundle()
                bundle.run {
                    putDouble("long", it.longitude)
                    putDouble("lat", it.latitude)
                    imap.putExtras(this)
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    startActivity(imap)
                }
            } else {
                toast(baseContext, "Location not availaible!")
            }
        }
    }

    private fun openSettings() {
        with(Intent()) {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", packageName, null)
            data = uri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(this)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    inner class NetworkChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "Receieved notification about network status")
            isNetworkAvailable(context)
        }

        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivity = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            //val info = connectivity.activeNetworkInfo
            val capabilities =
                connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        execConnected()
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        execConnected()
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        execConnected()
                        return true
                    }
                    else -> {
                        execNotConnected()
                        return false
                    }
                }
            }
            execNotConnected()
            return false
        }
    }

    private fun execConnected() {
        when {
            !isConnected -> {
                Log.v(LOG_TAG, "Now you are connected to Internet!")
                collapse(binding.banner)
                Handler(this@Self.mainLooper).postDelayed({ updateLocationUI() }, 500L)
                isConnected = true
            }
            else -> return
        }
    }

    private fun execNotConnected() {
        Log.v(LOG_TAG, "You are not connected to Internet!")
        Handler(this@Self.mainLooper).postDelayed({ expand(binding.banner) }, 500L)
        isConnected = false
    }

    private fun toggleFabMode(v: View) {
        rotate = ViewAnimation.rotateFab(v, !rotate)
        with(binding.fabs) {
            binding.backDrop.let {
                when {
                    rotate -> {
                        ViewAnimation.showIn(lytDashboard)
                        ViewAnimation.showIn(lytAccount)
                        it.setVisibility(View.VISIBLE)
                    }
                    else -> {
                        ViewAnimation.showOut(lytDashboard)
                        ViewAnimation.showOut(lytAccount)
                        it.setVisibility(View.GONE)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        with(builder) {
            setTitle(R.string.app_name)
            setIcon(R.mipmap.ic_launcher)
            setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finishAffinity()
                }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            val alert = create()
            alert.show()
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
                showHelpDialog()
                return true
            }
            1 -> {
                updateLocationUI()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        private val TAG = Self::class.java.simpleName
        private const val KEY_FILE_URI = "key_file_uri"

        // location updates interval - 5sec
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

        // fastest updates interval - 1 sec
        // location updates will be received if another app is requesting the locations
        // than your app can handle
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000
        private const val REQUEST_CHECK_SETTINGS = 100
        private const val LOG_TAG = "CheckNetworkStatus"
        val AD_UNIT_ID = "ca-app-pub-4788166230960873/7594432026"

        @JvmField
        var ad1: String? = null

        @JvmField
        var ad2: String? = null

        @JvmField
        var ad3: String? = null

        @JvmField
        var ad4: String? = null

        @JvmField
        var ad5: String? = null

        @JvmField
        var ad6: String? = null

        @JvmField
        var ad7: String? = null

        @JvmField
        var ad8: String? = null

        @JvmField
        var ad9: String? = null

        @JvmField
        var ad10: String? = null

    }

}

// private fun Any.observe(self: Self, premiumPurchasedObserver: Observer<Boolean>) {}
