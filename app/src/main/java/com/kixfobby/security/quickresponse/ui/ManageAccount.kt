package com.kixfobby.security.quickresponse.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.AnimationHelper
import com.kixfobby.security.quickresponse.helper.DateTimeHelper
import com.kixfobby.security.quickresponse.helper.Resolver
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import java.util.*


class ManageAccount : BaseActivity(), View.OnClickListener {

    private var mEditTextEmail: EditText? = null
    private var mEditTextPassword: EditText? = null
    private var mEditTextEmailReset: EditText? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private val user: FirebaseUser? = null
    private var user1: FirebaseUser? = null
    private var mCardViewProfile: CardView? = null
    private var mTextViewProfile: TextView? = null
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var email: String? = null
    private var name: String? = null
    private var phone: String? = null
    private var country: String? = null
    private var state: String? = null
    private var zip: String? = null
    private var bottomNavigationView: BottomNavigationView? = null
    private val nested_scroll_view: NestedScrollView? = null
    private val imageButtonToggleText1: ImageButton? = null
    private val imageButtonToggleText2: ImageButton? = null
    private val imageButtonToggleText3: ImageButton? = null
    private val layoutExpandText1: View? = null
    private val layoutExpandText2: View? = null
    private val layoutExpandText3: View? = null
    private var parent_view: View? = null
    var pNumber: String = "null"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_user)
        grantPermissions(this)
        autoSetOverlayPermission(this, "com.kixfobby.security.quickresponse")
        parent_view = findViewById(android.R.id.content)
        initComponent()
        supportActionBar!!.setTitle(R.string.manage_account)
        //Pref(this).put("purcahe_number", "qr_2users_product_id")
        //pNumber = Pref(this).get("purcahe_number", "qr_2users_product_id").toString()
        if (FirebaseAuth.getInstance().currentUser != null) {
            checkPaidUser()
        } /*else {
            startActivity(Intent(baseContext, RegisterAccount::class.java))
        }*/
    }

    private fun initComponent() {
        db = PageDatabase(this)
        c = db!!.allPages
        row = c!!.moveToLast()
        if (row) {
            id = c!!.getString(0)
            page = c!!.getString(1)
        }
        db!!.updatePage(Constants.ACCOUNT)
        email = Pref(this@ManageAccount).get("email", email)
        phone = Pref(this@ManageAccount).get("phone", phone)
        country = Pref(this@ManageAccount).get("cty", country)
        state = Pref(this@ManageAccount).get("state", state)
        zip = Pref(this@ManageAccount).get("zip", zip)
        name = Pref(this@ManageAccount).get("name", name)
        Companion.language = Pref(this@ManageAccount).get("language", Companion.language)
        mCardViewProfile = findViewById(R.id.card_view_profile)
        mTextViewProfile = findViewById(R.id.profile)
        mEditTextEmail = findViewById(R.id.field_email)
        mEditTextPassword = findViewById(R.id.field_password)
        mEditTextEmailReset = findViewById(R.id.field_email_reset)

        findViewById<View>(R.id.card_view_profile).setOnClickListener(this)
        findViewById<View>(R.id.update_email_button).setOnClickListener(this)
        findViewById<View>(R.id.update_password_button).setOnClickListener(this)
        findViewById<View>(R.id.send_password_reset_button).setOnClickListener(this)
        findViewById<View>(R.id.logout).setOnClickListener(this)
        findViewById<View>(R.id.delete).setOnClickListener(this)
        findViewById<View>(R.id.btn_manage_payments).setOnClickListener(this)
        findViewById<View>(R.id.nav_btn_1).setOnClickListener(this)
        findViewById<View>(R.id.nav_btn_2).setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = databaseReference!!.child(user1!!.getUid())
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //Toast.makeText(getApplicationContext(), "Not Null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            updateUI(user)
        }
        findViewById<View>(R.id.activity_expansion_lyt_expand_text_1).visibility = View.GONE
        findViewById<View>(R.id.activity_expansion_panels_btn_toggle_text_1).setOnClickListener {
            toggleSectionText1(findViewById(R.id.activity_expansion_panels_btn_toggle_text_1))
        }
        findViewById<View>(R.id.activity_expansion_panels_btn_hide_text_1).setOnClickListener {
            toggleSectionText1(findViewById(R.id.activity_expansion_panels_btn_toggle_text_1))
        }
        findViewById<View>(R.id.activity_expansion_lyt_expand_text_2).visibility = View.GONE
        findViewById<View>(R.id.activity_expansion_panels_btn_toggle_text_2).setOnClickListener {
            toggleSectionText2(findViewById(R.id.activity_expansion_panels_btn_toggle_text_2))
        }
        findViewById<View>(R.id.activity_expansion_panels_btn_hide_text_2).setOnClickListener {
            toggleSectionText2(findViewById(R.id.activity_expansion_panels_btn_toggle_text_2))
        }
        findViewById<View>(R.id.activity_expansion_lyt_expand_text_3).visibility = View.GONE
        findViewById<View>(R.id.activity_expansion_panels_btn_toggle_text_3).setOnClickListener {
            toggleSectionText3(findViewById(R.id.activity_expansion_panels_btn_toggle_text_3))
        }
        findViewById<View>(R.id.activity_expansion_panels_btn_hide_text_3).setOnClickListener {
            toggleSectionText3(findViewById(R.id.activity_expansion_panels_btn_toggle_text_3))
        }
    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(view: View) {
        val user = mAuth!!.currentUser
        when (view.id) {
            R.id.update_email_button -> if (validateEmail(mEditTextEmail!!)) {
                updateEmail(user!!)
            }
            R.id.update_password_button -> if (validatePassword()) {
                updatePassword(user!!)
            }
            R.id.send_password_reset_button -> if (validateEmail(mEditTextEmailReset!!)) {
                sendPasswordReset()
            }
            R.id.card_view_profile -> if (user != null) {
                infoDialog()
            }
            R.id.logout -> if (user != null) {
                Handler(mainLooper).postDelayed({
                    //showProgressIndicator();
                }, 3000)
                signOut()
            }
            R.id.delete -> if (user != null) {
                val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
                alert.setMessage(getString(R.string.delete) + Objects.requireNonNull(user).email + "?")
                alert.setCancelable(false)
                alert.setPositiveButton(android.R.string.yes) { dialogInterface, i -> deleteUser(user) }
                alert.setNegativeButton(android.R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }
                alert.show()
            }
            R.id.btn_manage_payments -> if (user != null) {
                startActivity(Intent(this, PaidUsersActivity::class.java))
            }
            R.id.nav_btn_1 -> if (user != null) {
                val intent = Intent(this, Self::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            R.id.nav_btn_2 -> if (user != null) {
                startActivity(Intent(this, Dashboard::class.java))
                //overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            mTextViewProfile!!.text = getString(R.string.hint_email) + ": " + user.email
            mTextViewProfile!!.append("\n")
            findViewById<View>(R.id.update_email_fields).visibility = View.VISIBLE
            findViewById<View>(R.id.update_password_fields).visibility = View.VISIBLE
            findViewById<View>(R.id.send_password_reset_fields).visibility = View.VISIBLE
            //findViewById(R.id.delete_fields).setVisibility(View.VISIBLE);
        } else {
            mTextViewProfile!!.setText(R.string.signed_out)
            findViewById<View>(R.id.update_email_fields).visibility = View.GONE
            findViewById<View>(R.id.update_password_fields).visibility = View.GONE
            findViewById<View>(R.id.send_password_reset_fields).visibility = View.GONE
        }
        hideProgressDialog()
    }

    private fun updateEmail(user: FirebaseUser) {
        showProgressDialog()
        reference1!!.child("Email Update").setValue(date)
        user.updateEmail(mEditTextEmail!!.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mTextViewProfile!!.setTextColor(Color.DKGRAY)
                //mTextViewProfile.setText(getString(R.string.updated, "User email"));
            } else {
                mTextViewProfile!!.setTextColor(Color.RED)
                mTextViewProfile!!.text = task.exception!!.message
            }
            hideProgressDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun updatePassword(user: FirebaseUser) {
        //showProgressDialog();
        Handler(mainLooper).postDelayed({
            //showProgressIndicator();
        }, 3300)
        reference1!!.child("Password Update").setValue(DateTimeHelper().current)
        user.updatePassword(mEditTextPassword!!.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.isSuccessful) {
                    mTextViewProfile!!.setTextColor(Color.DKGRAY)
                    // mTextViewProfile.setText(getString(R.string.updated, "User password"));
                } else {
                    mTextViewProfile!!.setTextColor(Color.RED)
                    mTextViewProfile!!.text = task.exception!!.message
                }
                hideProgressDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun sendPasswordReset() {
        showProgressDialog()
        reference1!!.child("Password Reset").setValue(DateTimeHelper().current)
        mAuth!!.sendPasswordResetEmail(mEditTextEmailReset!!.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mTextViewProfile!!.setTextColor(Color.DKGRAY)
                mTextViewProfile!!.setText(R.string.email_sent)
            } else {
                mTextViewProfile!!.setTextColor(Color.RED)
                mTextViewProfile!!.text = task.exception!!.message
            }
            hideProgressDialog()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteUser(user: FirebaseUser) {
        showProgressDialog()
        reference1!!.child("Deleted").setValue(DateTimeHelper().current)
        reference1!!.removeValue()
        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mTextViewProfile!!.setTextColor(Color.DKGRAY)
                mTextViewProfile!!.setText(R.string.user_account_deleted)
                Handler(mainLooper).postDelayed({ }, 3300)
                startActivity(Intent(baseContext, RegisterAccount::class.java))
                finishAffinity()
                Pref(this@ManageAccount).put("isLoggedIn", false)
                Pref(this@ManageAccount).clear()
            } else {
                mTextViewProfile!!.setTextColor(Color.RED)
                mTextViewProfile!!.text = task.exception!!.message
            }
            hideProgressDialog()
        }
    }

    private fun validateEmail(edt: EditText): Boolean {
        val email = edt.text.toString()
        return if (TextUtils.isEmpty(email)) {
            edt.error = "Email Required."
            false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edt.error = "Invalid."
            false
        } else {
            edt.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = mEditTextPassword!!.text.toString()
        return if (TextUtils.isEmpty(password)) {
            mEditTextPassword!!.error = "Password Required."
            false
        } else {
            mEditTextPassword!!.error = null
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun signOut() {
        val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        alert.setMessage(R.string.logout)
        alert.setCancelable(false)
        alert.setPositiveButton(android.R.string.yes) { dialogInterface, i ->
            reference1!!.child("Logged In").setValue("No")
            reference1!!.child("Logged Out").setValue("Yes: on " + DateTimeHelper().current)
            mAuth!!.signOut()
            startActivity(Intent(baseContext, RegisterAccount::class.java))
            finishAffinity()
            Pref(this@ManageAccount).put("isLoggedIn", false)
            Pref(this@ManageAccount).clear()
        }
        alert.setNegativeButton(android.R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }
        alert.show()
    }

    @SuppressLint("SetTextI18n")
    private fun infoDialog() {
        val user = mAuth!!.currentUser
        val dialog = Dialog(this, R.style.CustomDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog)
        dialog.setCancelable(false)
        (dialog.findViewById<View>(R.id.title) as TextView).setText(R.string.user_profile_info)
        (dialog.findViewById<View>(R.id.tEmail) as TextView).text = user!!.email
        (dialog.findViewById<View>(R.id.tPhone) as TextView).text = phone
        (dialog.findViewById<View>(R.id.tCountry) as TextView).text = country
        (dialog.findViewById<View>(R.id.tState) as TextView).text = state
        (dialog.findViewById<View>(R.id.tZip) as TextView).text = zip
        (dialog.findViewById<View>(R.id.tName) as TextView).text = user.displayName
        (dialog.findViewById<View>(R.id.tLanguage) as TextView).text = Resolver.getLang(this@ManageAccount, Companion.language!!)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        dialog.findViewById<View>(R.id.bt_close).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<View>(R.id.btn_edit).setOnClickListener { startActivity(Intent(this, FormEdit::class.java)) }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun toggleSectionText1(view: View) {
        val show = toggleArrow1(view)
        if (show) AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_1)).start()
        else AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_1)).start()
    }

    fun toggleArrow1(view: View): Boolean {
        return if (view.rotation == 0f) {
            view.animate().setDuration(200).rotation(180f)
            true
        } else {
            view.animate().setDuration(200).rotation(0f)
            false
        }
    }

    private fun toggleSectionText2(view: View) {
        val show = toggleArrow2(view)
        if (show) AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_2)).start()
        else AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_2)).start()
    }

    fun toggleArrow2(view: View): Boolean {
        return if (view.rotation == 0f) {
            view.animate().setDuration(200).rotation(180f)
            true
        } else {
            view.animate().setDuration(200).rotation(0f)
            false
        }
    }

    private fun toggleSectionText3(view: View) {
        val show = toggleArrow3(view)
        if (show) AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_3)).start()
        else AnimationHelper.fade(findViewById(R.id.activity_expansion_lyt_expand_text_3)).start()
    }

    fun toggleArrow3(view: View): Boolean {
        return if (view.rotation == 0f) {
            view.animate().setDuration(200).rotation(180f)
            true
        } else {
            view.animate().setDuration(200).rotation(0f)
            false
        }
    }

    override fun onResume() {
        super.onResume()
        grantPermissions(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    companion object {
        private const val TAG = "ManageUserActivity"
        private var language: String? = null
        //var purchaseNumber = pNumber
    }
}