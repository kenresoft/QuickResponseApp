package com.kixfobby.security.quickresponse.ui

import android.app.KeyguardManager
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.DateTimeHelper
import com.kixfobby.security.quickresponse.home.Nearby
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.receiver.Restarter
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import java.io.File

class SplashActivity : BaseActivity() {
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private var user: FirebaseUser? = null

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        //setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        db = PageDatabase(this)
        c = db!!.allPages
        row = c!!.moveToLast()
        if (row) {
            id = c!!.getString(0)
            page = c!!.getString(1)
        }
        mAuth = FirebaseAuth.getInstance()
        user = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        val keyGuard = Pref(this).get("security", false)
        if (keyGuard) {
            val km = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
            if (km.isKeyguardSecure) {
                val i = km.createConfirmDeviceCredentialIntent("Phone Authentication required", "password")
                startActivityForResult(i, CODE_AUTHENTICATION_VERIFICATION)
            } else {
                toast(
                    this,
                    "No any security setup done by user (pattern or password or pin or fingerprint)")
                startActivityForResult(Intent(Settings.ACTION_SECURITY_SETTINGS), 123)
            }
        } else startUi()
        if (user != null) {
            reference1 = databaseReference!!.child(user!!.uid)
            reference1!!.child("Launched").setValue(DateTimeHelper().current)
        }
    }

    private fun startUi() {
        Handler().postDelayed({
            val isLoggedIn =
                Pref(this).get("isLoggedIn", false)
            if (isLoggedIn) {
                when (page) {
                    Constants.FIRE -> startActivity(Intent(this, Fire::class.java))
                    Constants.NEARBY -> startActivity(Intent(this, Nearby::class.java))
                    Constants.SELF -> startActivity(Intent(this, Self::class.java))
                    Constants.DASHBOARD -> startActivity(Intent(this, Dashboard::class.java))
                    Constants.ACCOUNT -> startActivity(Intent(this, ManageAccount::class.java))
                    else -> {
                        toast(this, "Welcome Back!")
                        startActivity(Intent(this, Self::class.java))
                    }
                }
            } else {
                if (Constants.FORM == page) {
                    startActivity(Intent(this, Form::class.java))
                } else {
                    startActivity(Intent(this, RegisterAccount::class.java))
                }
            }
            finish()
        }, 2500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CODE_AUTHENTICATION_VERIFICATION) {
            startUi()
            toast(this, "Verified user's identity")
        } else {
            toast(this, "Unable to verify user's identity")
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val directory = File(BaseActivity.Companion.storageLocation)
        if (!directory.exists()) {
            directory.mkdir()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onStop() {
        super.onStop()
        val broadcastIntent1 = Intent(this, Restarter::class.java)
        sendBroadcast(broadcastIntent1)

        val broadcastIntent2 = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent2)
    }

    companion object {
        private const val TAG = "SplashActivity"
        private const val CODE_AUTHENTICATION_VERIFICATION = 241
    }
}