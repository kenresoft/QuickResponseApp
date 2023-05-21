package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.FormBinding
import com.kixfobby.security.quickresponse.helper.CountryHelper
import com.kixfobby.security.quickresponse.helper.DateTimeHelper
import com.kixfobby.security.quickresponse.helper.LanguageHelper
import com.kixfobby.security.quickresponse.helper.Resolver
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import java.util.*

class Form : BaseActivity(), View.OnClickListener {
    private var progressView: RelativeLayout? = null
    private var db: PageDatabase? = null
    private var pC: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private var user: FirebaseUser? = null
    private var user1: FirebaseUser? = null
    private var email: String? = null
    private var country: String? = null
    private var ctyy: String? = null
    private val ctyKey: String? = null
    private var ctryKey: String? = null
    private var langKey: String? = null
    private var ctryKeyFull: String? = null
    private var langKeyFull: String? = null
    private val state: String? = null
    private val zip: String? = null
    private val phone: String? = null
    private var ck: String? = null
    private var lk: String? = null
    private var cbk: String? = null
    private var lbk: String? = null
    private var cik: String? = null
    private val co: String? = null
    private var bundle: Bundle? = null
    private lateinit var binding: FormBinding

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        bundle = intent.extras
        progressView = findViewById(R.id.view)
        db = PageDatabase(this)
        pC = db!!.allPages
        row = pC!!.moveToLast()
        if (row) {
            id = pC!!.getString(0)
            page = pC!!.getString(1)
        }
        db!!.updatePage(Constants.FORM)
        //Pref(this@Form).put("isFormPage", true)
        supportActionBar!!.setTitle(R.string.user_info)

        var verified = Pref(this).get("isFormPage", false).toString()
        if (verified.equals("false")) {
            var intent = Intent(this, RegisterAccount::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        if (user1 != null) {
            reference1 = databaseReference!!.child(user1!!.uid)
            Pref(this@Form).put("uid", user1!!.uid)
        }
        mAuthListener = AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
                reference1 = databaseReference!!.child(user1!!.uid)
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            if (user != null) email = user!!.email
        }

        if (user != null) {
            reference1!!.child("Logged In").setValue("Yes: on " + DateTimeHelper().current!!)
            reference1!!.child("Logged Out").setValue("No")
            reference1!!.child("Verified").setValue(user1!!.isEmailVerified)
        }
        //init();
        with(binding) {
            etCountry.setOnClickListener(this@Form)
            etLanguage.setOnClickListener(this@Form)
            btSubmit.setOnClickListener(this@Form)

            if (bundle != null) {
                try {
                    country = bundle!!.getString("count")
                    language = bundle!!.getString("lang")
                    lbk = bundle!!.getString("lang")
                    cbk = bundle!!.getString("ctry")
                    cik = bundle!!.getString("iso")
                    ck = bundle!!.getString("ctyy")
                    lk = bundle!!.getString("language")
                    if (country != null) {
                        if (country!!.contains("NG -")) {
                            //etZip.hint = getString(R.string.local_government)
                            etZip.inputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
                            etState.isFocusable = false
                            etState.setText(state)
                            etState.setCompoundDrawables(
                                null,
                                null,
                                ContextCompat.getDrawable(this@Form, R.drawable.ic_arrow_drop),
                                null
                            )
                            etState.setOnClickListener { showStateDialog() }
                        } else {
                            etZip.hint = getString(R.string.zip_code)
                            etState.isFocusable = true
                            etState.setText(state)
                            etState.setCompoundDrawables(null, null, null, null)
                        }
                    }
                    if (country != null) {
                        etCountry.setText(country)
                    }
                    if (language != null) {
                        etLanguage.setText(language)
                        //Pref(Form.this).put("lang", language);
                    } //else Toast.makeText(this@Form ,language, Toast.LENGTH_SHORT).show();
                    etState.setText(state)
                    etZip.setText(zip)
                    etPhone.setText(phone)
                } catch (e: Exception) {
                    //Toast.makeText(this@Form ,"Bundle Exception", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    override fun onClick(v: View) {
        with(binding) {
            when (v.id) {
                R.id.et_country -> showCountryDialog()
                R.id.et_language -> showLanguageDialog()
                R.id.bt_submit -> if (!user!!.isEmailVerified) {
                    val alert = AlertDialog.Builder(this@Form, R.style.CustomDialogTheme)
                    alert.setTitle(R.string.verify_email_to_continue)
                    alert.setMessage("Re-SignIn after verifying your your email")
                    alert.setCancelable(false)
                    alert.setPositiveButton(R.string.open_email) { _, _ ->
                        db!!.updatePage(Constants.LOGIN)
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory("android.intent.category.APP_EMAIL")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.open_email_for_verification_link)
                            )
                        )
                        finish()
                    }
                    alert.setNegativeButton("Re-SignIn") { dialogInterface, _ ->
                        db!!.updatePage(Constants.LOGIN)
                        startActivity(Intent(baseContext, RegisterAccount::class.java))
                        finish()
                        dialogInterface.dismiss()
                    }
                    alert.show()

                    //Pref(Form.this).edit().putBoolean("verify", false);
                } else {
                    val reference1 = databaseReference!!.child(user!!.uid)
                    reference1.child("Verified").setValue(user!!.isEmailVerified)
                    when {
                        TextUtils.isEmpty(etLanguage.text.toString()) || TextUtils.isEmpty(etCountry.text.toString()) || etState.text.toString() == "" || etZip.text.toString() == "" || etPhone.text.toString() == "" -> {
                            toast(this@Form, getString(R.string.fill_form_completely))
                        }
                        etZip.text.toString().length <= 4 -> {
                            toast(this@Form, "Please, enter valid Zip/LGA.")
                        }
                        etPhone.text.toString().length <= 6 -> {
                            toast(this@Form, "Please, enter valid Phone Number.")
                        }
                        else -> {
                            if (isSaved) {
                                progressView!!.visibility = View.VISIBLE
                                findViewById<View>(R.id.tv).animation =
                                    AnimationUtils.loadAnimation(this@Form, R.anim.blink)
                                etLanguage.isEnabled = false
                                etCountry.isEnabled = false
                                etState.isEnabled = false
                                etZip.isEnabled = false
                                etPhone.isEnabled = false
                                etLanguage.isClickable = false
                                etCountry.isClickable = false
                                etState.isClickable = false
                                etZip.isClickable = false
                                etPhone.isClickable = false
                                onBackPressed()
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(Intent(this@Form, LanguageHelper::class.java))
                                    finish()
                                }, 4000)
                                Pref(this@Form).put("isLoggedIn", true)
                            } else toast(this@Form, getString(R.string.details_not_saved_check_values))
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun trim(str: String): String {
        return str.replace(" ", "")
    }

    private fun showStateDialog() {
        val ch: Array<out String> = application.resources.getStringArray(R.array.states_nga)
        val builder = AlertDialog.Builder(this@Form, R.style.CustomDialogTheme)
        builder.setTitle("State")
        builder.setSingleChoiceItems(ch, -1) { dialogInterface, k ->
            binding.etState.setText(ch[k])
            dialogInterface.dismiss()
        }
        builder.show()
    }

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
            Pref(this).put("isFormPage", false)
            Pref(this@Form).put("isLoggedIn", false)
            Pref(this@Form).clear()
        }
        alert.setNegativeButton(android.R.string.no) { dialogInterface, i -> dialogInterface.dismiss() }
        alert.show()
    }


    //Toast.makeText(this@Form ,"CTY NULL", Toast.LENGTH_SHORT).show();
    private val isSaved: Boolean
        get() {
            with(binding) {
                val lang2 = Pref(this@Form).get("lang", language)
                val ctry2 = Pref(this@Form).get("ctry", country)
                val cty2 = Pref(this@Form).get("cty", co)
                val state = etState.text.toString().trim { it <= ' ' }
                val zip = etZip.text.toString().trim { it <= ' ' }
                val phone = etPhone.text.toString().trim { it <= ' ' }
                Pref(this@Form).put("email", email)
                Pref(this@Form).put("lang", lang2)
                Pref(this@Form).put("ctry", ctry2)
                Pref(this@Form).put("state", state)
                Pref(this@Form).put("zip", zip)
                Pref(this@Form).put("phone", phone)
                if (cty2 != null) {
                    Pref(this@Form).put("cty", cty2)
                }
                reference1!!.child("Language").setValue(lang2)
                reference1!!.child("Country").setValue(cty2)
                reference1!!.child("State").setValue(state)
                reference1!!.child("Zip").setValue(zip)
                reference1!!.child("Phone").setValue(phone)
                return true
            }
        }

    private fun showCountryDialog() {
        val cList = CountryHelper.get()
        val sList = ArrayList<String>()
        for (i in cList.indices) {
            val item = cList[i]
            sList.add(item.toString())
        }
        val ch = sList.toTypedArray<CharSequence>()
        val builder = AlertDialog.Builder(this@Form, R.style.CustomDialogTheme)
        builder.setTitle(R.string.country_title)
        builder.setSingleChoiceItems(ch, -1) { dialogInterface, k ->
            try {
                with(binding) {
                    val i = ch[k].toString().indexOf("(", 5)
                    val i2 = ch[k].toString().indexOf(")", 5)
                    val i3 = ch[k].toString().indexOf("-", 1)
                    ctyy = ch[k].toString().substring(i3 + 2, i)
                    ctryKey = ch[k].toString().substring(i + 1, i2)
                    ctryKeyFull = ch[k].toString().substring(0, i)
                    val iso3 = ch[k].toString().substring(0, i3)
                    etCountry.setText(ctyy)

                    Pref(this@Form).put("iso", iso3)
                    Pref(this@Form).put("cty", ctyy)
                    Pref(this@Form).put("country", ctryKey)
                    Pref(this@Form).put("email", email)
                    Pref(this@Form).put("ctry", ctryKeyFull)
                    Pref(this@Form).put("state", etState.text.toString().trim { it <= ' ' })
                    Pref(this@Form).put("zip", etZip.text.toString().trim { it <= ' ' })
                    Pref(this@Form).put("phone", etPhone.text.toString().trim { it <= ' ' })

                    toast(baseContext, "$ctryKeyFull - $ctryKey")
                    val mIntent = Intent(this@Form, Form::class.java)
                    mIntent.putExtra("count", ctryKeyFull)
                    langKeyFull = etLanguage.text.toString().trim { it <= ' ' }
                    mIntent.putExtra("lang", langKeyFull)
                    mIntent.putExtra("lang", lbk)
                    mIntent.putExtra("ctry", cbk)
                    mIntent.putExtra("iso", cik)
                    mIntent.putExtra("ctyy", ck)
                    mIntent.putExtra("language", lk)
                    startActivity(mIntent)
                    overridePendingTransition(0, 0)
                    finish()
                    dialogInterface.dismiss()
                }
            } catch (e: Exception) {
                //android.widget.Toast.makeText(Form.this@Form ,"Country Exception", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
        builder.show()
    }

    private fun showLanguageDialog() {
        val ch = application.resources.getTextArray(R.array.languages)
        val builder = AlertDialog.Builder(this@Form, R.style.CustomDialogTheme)
        builder.setTitle(R.string.language_title)
        builder.setSingleChoiceItems(ch, -1) { dialogInterface, k ->
            try {
                with(binding) {
                    val lang = ch[k].toString()
                    langKey = Resolver.getLangKey(this@Form, lang)
                    setLanguage(langKey!!)
                    langKeyFull = Resolver.getLang(this@Form, langKey!!)
                    etLanguage.setText(langKeyFull)
                    val `is` = Pref(this@Form).get("iso", "iso")!!
                        .trim { it <= ' ' }
                    val ct = Locale("en", `is`).displayCountry
                    val str = "$`is` - $ct"
                    if (str.contains("iso - ISO")) etCountry.setText("") else etCountry.setText(str)
                    Pref(this@Form).put("language", langKey)

                    Pref(this@Form).put("lang", langKeyFull)

                    toast(baseContext, langKeyFull!!)
                    dialogInterface.dismiss()
                }
            } catch (e: Exception) {
                //android.widget.Toast.makeText(Form.this@Form ,"Language Exception", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "Resend Verification Link").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_mail_outline_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 1, Menu.NONE, "LogOut")
            .setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_baseline_login_24))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            if (!user!!.isEmailVerified) {
                val alert = AlertDialog.Builder(this@Form, R.style.CustomDialogTheme)
                alert.setMessage("Didn't Receive link or it's Expired?")
                alert.setCancelable(false)
                alert.setPositiveButton("Re-send Link") { _, _ ->
                    val firebaseUser = mAuth!!.currentUser
                    firebaseUser!!.sendEmailVerification().addOnCompleteListener(this@Form) { task ->
                        if (task.isSuccessful) {
                            toast(this@Form, getString(R.string.verification_email_sent_to) + firebaseUser.email)
                        } else {
                            toast(this@Form, task.exception!!.message!!)
                        }
                    }
                }
                alert.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
                alert.show()
            } else toast(this@Form, "User already verified!")
            return true
        }
        if (item.itemId == 1) {
            //signOut()
            val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            alert.setMessage(R.string.logout)
            alert.setCancelable(false)
            alert.setPositiveButton(android.R.string.yes) { _, _ ->
                reference1!!.child("Logged In").setValue("No")
                reference1!!.child("Logged Out").setValue("Yes: on " + DateTimeHelper().current)
                mAuth!!.signOut()
                startActivity(Intent(baseContext, RegisterAccount::class.java))
                finishAffinity()
                Pref(this@Form).put("isLoggedIn", false)
                Pref(this@Form).clear()
            }
            alert.setNegativeButton(android.R.string.no) { dialogInterface, _ -> dialogInterface.dismiss() }
            alert.show()
        }
        return super.onOptionsItemSelected(item)
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

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        /*if (!isSubmitted) {
            if (data.getAllData().getCount() > 0)
                data.deleteData(email);
        }*/
    }

    companion object {
        private val TAG = Form::class.java.name
        private var language: String? = null
    }
}