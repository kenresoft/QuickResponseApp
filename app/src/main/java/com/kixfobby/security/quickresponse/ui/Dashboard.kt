package com.kixfobby.security.quickresponse.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.BaseApplication
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.BaseGridAdapter
import com.kixfobby.security.quickresponse.databinding.ActivityDashboardBinding
import com.kixfobby.security.quickresponse.home.HomeActivity
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.model.DashItem
import com.kixfobby.security.quickresponse.receiver.Restarter
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.DatabaseAccess
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.widget.AnimTextView
import com.kixfobby.security.quickresponse.widget.Appirater
import com.kixfobby.security.quickresponse.widget.GridItemDecoration
import java.util.*

class Dashboard : BaseActivity(), View.OnClickListener {

    private val appPackageName: String = "com.kixfobby.security.quickresponse"
    var databaseAccess: DatabaseAccess? = null
    var tipTitle: ArrayList<String>? = null
    var detail: String? = null
    var photo: ArrayList<Drawable>? = null
    var imageView: ImageView? = null
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private var user: FirebaseUser? = null
    private lateinit var binding: ActivityDashboardBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        Appirater.appLaunched(this)
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser

        if (user != null) {
            setContentView(view)
            initUi()
            initComponent()
            initCount()
            checkPaidUser()
        } else {
            startActivity(Intent(baseContext, RegisterAccount::class.java))
        }
    }

    private fun initUi() {

        //showNotifAlert()

        if (!verifyInstaller(this)) {
            /*showProgressDialog();*/
            return
        }
        supportActionBar!!.setTitle(R.string.dashboard)

        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = databaseReference!!.child(user!!.uid)
        reference1!!.child("Click Count").setValue(0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (intent.extras != null) {
            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!![key]
                Log.d(TAG, "Key: $key Value: $value")
            }
        }
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }

            // Get new Instance ID token
            val token = task.result.token

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            reference1!!.child("Token Id").setValue(token)
        })
    }

    private fun initCount() {
        imageView = findViewById(R.id.bg_image)
        //note that you can use getPreferences(MODE_PRIVATE), but this is easier to use from Fragments.
        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
        var appOpenedCount = prefs.getInt("app_opened_count", 1)
        if (!(application as BaseApplication).wasLastConfigChangeRecent(10000)) //within 10 seconds - a huge buffer
        {
            appOpenedCount += 1
            prefs.edit().putInt("app_opened_count", appOpenedCount).apply()
        }
        val count5 = appOpenedCount % 5 == 0
        val count8 = appOpenedCount % 8 == 0
        if (count5) {
            with(imageView) { this?.setImageResource(R.drawable.material_bg_4) }
            appOpenedCount += 1
            prefs.edit().putInt("app_opened_count", appOpenedCount).apply()
        }
    }

    private fun initComponent() {
        with(binding) {
            db = PageDatabase(this@Dashboard)
            c = db!!.allPages
            row = c!!.moveToLast()
            if (row) {
                id = c!!.getString(0)
                page = c!!.getString(1)
            }
            db!!.updatePage(Constants.DASHBOARD)
            databaseAccess = DatabaseAccess.getInstance(this@Dashboard)
            tipTitle = databaseAccess!!.tipTitle
            //photo = databaseAccess!!.getTipPhotos(baseContext)

            headTv.animation = AnimationUtils.loadAnimation(this@Dashboard, R.anim.blink)
            animTv.setInAnimation(AnimationUtils.loadAnimation(this@Dashboard, R.anim.cover));
            animTv.setOutAnimation(AnimationUtils.loadAnimation(this@Dashboard, R.anim.cover));
            animTv.setTypeface(ResourcesCompat.getFont(this@Dashboard, R.font.paghetti))
            animTv.setDuration(7000, AnimTextView.MILLISECONDS)
            animTv.setTexts(tipTitle!!)
            animTv.setAnimate(true)
            animTv.setOnClickListener(this@Dashboard)

            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = GridLayoutManager(this@Dashboard, 2)
            recyclerView.addItemDecoration(GridItemDecoration(2, dpToPx(this@Dashboard, 8), true))
            recyclerView.setHasFixedSize(true)
            recyclerView.setItemViewCacheSize(20)
            recyclerView.isDrawingCacheEnabled = true
            recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            recyclerView.isNestedScrollingEnabled = false
            val items: ArrayList<DashItem> = ArrayList()
            with(items) {
                add(0, DashItem(R.drawable.baseline_add_alert_24, "Alert Home", 0))
                add(1, DashItem(R.drawable.baseline_contacts_24, getString(R.string.security_contacts), 1))
                add(2, DashItem(R.drawable.baseline_sms_24, "Message History", 2))
                add(3, DashItem(R.drawable.baseline_location_on_24, "Location History", 3))
                //add(4, DashItem(R.drawable.baseline_perm_media_24, "Media Files", 4))
                add(4, DashItem(R.drawable.baseline_settings_applications_24, "Settings", 4))
                add(5, DashItem(R.drawable.baseline_person_24, getString(R.string.title_account), 5))
                add(6, DashItem(R.drawable.baseline_privacy_tip_24, getString(R.string.privacy_policy), 6))
                add(7, DashItem(R.drawable.baseline_gavel_24, getString(R.string.terms_conditions), 7))
            }

            //set data and list adapter
            val mAdapter = BaseGridAdapter(this@Dashboard, items)
            recyclerView.adapter = mAdapter

            // on item list clicked
//        mAdapter.setOnItemClickListener(listener)

            val listener = object : BaseGridAdapter.OnItemClickListener {
                override fun onItemClick(view: View?, obj: DashItem?, position: Int) {
                    when (position) {
                        0 -> {
                            startActivity(Intent(baseContext, Self::class.java))
                            animate()
                        }
                        1 -> {
                            startActivity(Intent(baseContext, ContactsBaseActivity::class.java))
                            animate()
                        }
                        2 -> {
                            //startActivity(Intent(baseContext, MediaActivity::class.java))
                            startActivity(Intent(baseContext, SmsBaseActivity::class.java))
                            animate()
                        }
                        3 -> {
                            startActivity(Intent(baseContext, LocationBaseActivity::class.java))
                            animate()
                        }
                        4 -> {
                            //startActivity(Intent(baseContext, StorageActivity::class.java))
                            startActivity(Intent(baseContext, SettingsActivity::class.java))
                            animate()
                        }
                        5 -> {
                            startActivity(Intent(baseContext, ManageAccount::class.java))
                            animate()
                        }
                        6 -> {
                            animate()
                            showPrivacyDialog()
                        }
                        7 -> {
                            animate()
                            showTermsDialog()
                            //startActivity(Intent(baseContext, HomeActivity::class.java))
                        }
                    }
                }
            }
            mAdapter.setOnItemClickListener(listener)
            showUpdate()

            val set: Set<String>? = Pref(this@Dashboard).get("friends_set", setOf("Friend Set"))
            val mainList: ArrayList<String> = ArrayList()
            mainList.addAll(set!!)

        }
    }

    override fun onClick(p1: View) {
        if (p1.id.equals(R.id.animTv)) {
            val title = binding.animTv.text.toString()
            detail = databaseAccess!!.getTipDetail(title)

            val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            with(alert) {
                setTitle(title)
                setMessage(detail)
                setIcon(R.drawable.ic_launcher_background)
                //alert.setIcon(image);
                setCancelable(true)
                create().show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        animate()
        grantPermissions(this)
    }

    override fun onRestart() {
        super.onRestart()
        //animTextView!!.restart()
    }

    override fun onPause() {
        super.onPause()
        //animTextView!!.pause()
    }

    override fun onStop() {
        super.onStop()
        val broadcastIntent1 = Intent(this, Restarter::class.java)
        sendBroadcast(broadcastIntent1)

        val broadcastIntent2 = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent2)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        animate()
    }

    fun openSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /*R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }*/
            R.id.action_updates -> {
                startActivity(Intent(this, MessageActivity::class.java))
            }
            /*R.id.action_help -> {
                startActivity(Intent(this, HelpActivity::class.java))
            }*/
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.action_emergency -> {
                startActivity(Intent(this, EmergencyBaseActivity::class.java))
            }
            /*R.id.action_share -> {
            }*/
            R.id.action_rate -> {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                } catch (anfe: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "Dashboard"
        fun dpToPx(c: Context, dp: Int): Int {
            val r = c.resources
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics))
        }
    }
}