package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
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
import com.kixfobby.security.quickresponse.helper.LanguageHelper
import com.kixfobby.security.quickresponse.helper.Resolver
import com.kixfobby.security.quickresponse.storage.Pref
import java.util.*

class FormEdit : BaseActivity(), View.OnClickListener {
    //private var progressView: RelativeLayout? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private var user: FirebaseUser? = null
    private var user1: FirebaseUser? = null
    private var email: String? = null
    private var country: String? = null
    private var ctyy: String? = null
    private var ctyKey: String? = null
    private var ctryKey: String? = null
    private var langKey: String? = null
    private var ctryKeyFull: String? = null
    private var langKeyFull: String? = null
    private var state: String? = null
    private var zip: String? = null
    private var phone: String? = null
    private var ck: String? = null
    private var lk: String? = null
    private var cbk: String? = null
    private var lbk: String? = null
    private var cik: String? = null
    private var co: String? = null
    private var bundle: Bundle? = null
    private lateinit var binding: FormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FormBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        noBackPress = true
        bundle = intent.extras
        supportActionBar!!.setTitle(R.string.user_info)

        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = databaseReference!!.child(user1!!.uid)
        mAuthListener = AuthStateListener { firebaseAuth ->
            user = firebaseAuth.currentUser
            if (user != null) {
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user!!.uid)
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
            email = user!!.email
        }

        init()
        with(binding) {
            etCountry.setOnClickListener(this@FormEdit)
            etLanguage.setOnClickListener(this@FormEdit)
            btSubmit.setOnClickListener(this@FormEdit)

            //Toast.makeText(getBaseContext(), langKey, Toast.LENGTH_SHORT).show();
            if (ctyKey != null) {
                if (ctyKey == "NGA") {
                    etZip.hint = getString(R.string.local_government)
                    etState.isFocusable = false
                    etState.setText(state)
                    etState.setCompoundDrawables(
                        null,
                        null,
                        ContextCompat.getDrawable(this@FormEdit, R.drawable.ic_arrow_drop),
                        null
                    )
                    etState.setOnClickListener(View.OnClickListener { showStateDialog() })
                } else {
                    etZip.hint = getString(R.string.zip_code)
                    etState.isFocusable = true
                    etState.setText(state)
                    etState.setCompoundDrawables(null, null, null, null)
                }
            }
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
                            etState.isFocusable = false
                            etState.setText(state)
                            etState.setCompoundDrawables(
                                null,
                                null,
                                ContextCompat.getDrawable(this@FormEdit, R.drawable.ic_arrow_drop),
                                null
                            )
                            etState.setOnClickListener(View.OnClickListener { showStateDialog() })
                        } else {
                            //etZip.hint = getString(R.string.zip_code)
                            etState.isFocusable = true
                            etState.setText(state)
                            etState.setCompoundDrawables(null, null, null, null)
                        }
                    }
                    if (country != null) {
                        etCountry.setText(country)
                    }
                    if (Companion.language != null) {
                        etLanguage.setText(Companion.language)
                        //Pref(FormEdit.this).put("lang", language);
                    }
                    etState.setText(state)
                    etZip.setText(zip)
                    etPhone.setText(phone)
                } catch (e: Exception) {
                    //android.widget.Toast.makeText(this, "Bundle Exception", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private fun init() {
        with(binding) {
            //if (bundle == null) {
            language = Pref(this@FormEdit).get("lang", "langKeyFull")
            lbk = language
            ctyKey = Pref(this@FormEdit).get("ctry", "ctryKeyFull")
            cbk = ctyKey
            ctyKey = Pref(this@FormEdit).get("iso", "iso")
            cik = ctyKey
            ctyKey = Pref(this@FormEdit).get("ctyy", "ctyy")
            ck = ctyKey
            langKey = Pref(this@FormEdit).get("language", "langKey")
            lk = langKey
            //}
            co = Pref(this@FormEdit).get("ctry", "co")
            state = Pref(this@FormEdit).get("state", "state")
            zip = Pref(this@FormEdit).get("zip", "zip")
            phone = Pref(this@FormEdit).get("phone", "phone")
            etLanguage.setText(Resolver.getLang(this@FormEdit, langKey!!))
            etCountry.setText(co)
            etState.setText(state)
            etZip.setText(zip)
            etPhone.setText(phone)
        }
    }

    override fun onClick(v: View) {
        with(binding) {
            when (v.id) {
                R.id.et_country -> showCountryDialog()
                R.id.et_language -> showLanguageDialog()
                R.id.bt_submit -> if (!user!!.isEmailVerified) {
                    val alert = AlertDialog.Builder(this@FormEdit, R.style.CustomDialogTheme)
                    alert.setTitle(R.string.verify_email_to_continue)
                    alert.setMessage("Re-SignIn after verifying your your email")
                    alert.setCancelable(false)
                    alert.setPositiveButton(R.string.open_email) { _, _ ->
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory("android.intent.category.APP_EMAIL")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.open_email_for_verification_link)
                            )
                        )
                    }
                    alert.setNegativeButton("Re-SignIn") { dialogInterface, _ ->
                        startActivity(Intent(baseContext, RegisterAccount::class.java))
                        finish()
                        dialogInterface.dismiss()
                    }
                    alert.show()
                    //Pref(FormEdit.this).edit().putBoolean("verify", false);
                } else {
                    if (TextUtils.isEmpty(etLanguage.text.toString()) || TextUtils.isEmpty(etCountry.text.toString()) || etState.text.toString() == "" || etZip.text.toString() == "" || etPhone.text.toString() == "") {
                        Toast.makeText(baseContext, R.string.fill_form_completely, Toast.LENGTH_SHORT).show()
                    } else {
                        if (isSaved) {
                            view.visibility = View.VISIBLE
                            tv.animation = AnimationUtils.loadAnimation(this@FormEdit, R.anim.blink)
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
                                startActivity(Intent(this@FormEdit, LanguageHelper::class.java))
                                finish()
                            }, 4000)
                            Pref(this@FormEdit).put("isLoggedIn", true)
                        } else Toast.makeText(this@FormEdit, R.string.details_not_saved_check_values, Toast.LENGTH_LONG)
                            .show()
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
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle("State")
        builder.setSingleChoiceItems(ch, -1) { dialogInterface, k ->
            binding.etState.setText(ch[k])
            dialogInterface.dismiss()
        }
        builder.show()
    }

    //Toast.makeText(this, "CTY NULL", Toast.LENGTH_SHORT).show();
    private val isSaved: Boolean
        get() {
            with(binding) {
                val lang2 = Pref(this@FormEdit).get(
                    "lang",
                    language
                )
                val ctry2 = Pref(this@FormEdit).get("ctry", country)
                val cty2 = Pref(this@FormEdit).get("cty", co)
                val state = etState.text.toString().trim { it <= ' ' }
                val zip = etZip.text.toString().trim { it <= ' ' }
                val phone = etPhone.text.toString().trim { it <= ' ' }
                Pref(this@FormEdit).put("email", email)
                Pref(this@FormEdit).put("lang", lang2)
                Pref(this@FormEdit).put("ctry", ctry2)
                Pref(this@FormEdit).put("state", state)
                Pref(this@FormEdit).put("zip", zip)
                Pref(this@FormEdit).put("phone", phone)
                if (cty2 != null) {
                    Pref(this@FormEdit).put("cty", cty2)
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
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
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

                    Pref(this@FormEdit).put("iso", iso3)
                    Pref(this@FormEdit).put("cty", ctyy)
                    Pref(this@FormEdit).put("country", ctryKey)
                    Pref(this@FormEdit).put("email", email)
                    Pref(this@FormEdit).put("ctry", ctryKeyFull)
                    Pref(this@FormEdit).put("state", etState.text.toString().trim { it <= ' ' })
                    Pref(this@FormEdit).put("zip", etZip.text.toString().trim { it <= ' ' })
                    Pref(this@FormEdit).put("phone", etPhone.text.toString().trim { it <= ' ' })

                    Toast.makeText(baseContext, "$ctryKeyFull - $ctryKey", Toast.LENGTH_SHORT).show()
                    val mIntent = Intent(this@FormEdit, FormEdit::class.java)
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
                //android.widget.Toast.makeText(FormEdit.this, "Country Exception", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
        builder.show()
    }

    private fun showLanguageDialog() {
        val ch = application.resources.getTextArray(R.array.languages)
        val builder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        builder.setTitle(R.string.language_title)
        builder.setSingleChoiceItems(ch, -1) { dialogInterface, k ->
            try {
                with(binding) {
                    val lang = ch[k].toString()
                    langKey = Resolver.getLangKey(this@FormEdit, lang)
                    setLanguage(langKey!!)
                    langKeyFull = Resolver.getLang(this@FormEdit, langKey!!)
                    etLanguage.setText(langKeyFull)
                    val `is` = Pref(this@FormEdit).get("iso", "iso")!!.trim { it <= ' ' }
                    val ct = Locale("en", `is`).displayCountry
                    val str = "$`is` - $ct"
                    if (str.contains("iso - ISO")) etCountry.setText("") else etCountry.setText(str)
                    Pref(this@FormEdit).put("language", langKey)
                    Pref(this@FormEdit).put("lang", langKeyFull)
                    Toast.makeText(baseContext, langKeyFull, Toast.LENGTH_SHORT).show()
                    dialogInterface.dismiss()
                }
            } catch (e: Exception) {
                //Toast.makeText(FormEdit.this, "Language Exception", Toast.LENGTH_SHORT).show();
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
        menu.add(Menu.NONE, 1, Menu.NONE, "Back").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.ic_arrow_back
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            if (!user!!.isEmailVerified) {
                val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
                alert.setMessage("Didn't Receive link or it's Expired?")
                alert.setCancelable(false)
                alert.setPositiveButton("Re-send Link") { _, _ ->
                    val firebaseUser = mAuth!!.currentUser
                    firebaseUser
                        ?.sendEmailVerification()?.addOnCompleteListener(this@FormEdit) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@FormEdit,
                                    getString(R.string.verification_email_sent_to) + firebaseUser.email,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@FormEdit,
                                    task.exception!!.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
                alert.setNegativeButton(R.string.cancel) { dialogInterface, _ -> dialogInterface.dismiss() }
                alert.show()
            } else Toast.makeText(this, "User already verified!", Toast.LENGTH_SHORT).show()
            return true
        }
        if (item.itemId == 1) {
            Pref(this@FormEdit).put("lang", lbk)
            Pref(this@FormEdit).put("ctry", cbk)
            Pref(this@FormEdit).put("iso", cik)
            Pref(this@FormEdit).put("ctyy", ck)
            Pref(this@FormEdit).put("language", lk)

            var intent = Intent(this, ManageAccount::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
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
        private val TAG = FormEdit::class.java.name
        private var language: String? = null
    }
}