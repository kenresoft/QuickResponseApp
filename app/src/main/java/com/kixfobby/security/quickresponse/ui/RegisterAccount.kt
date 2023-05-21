package com.kixfobby.security.quickresponse.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.transition.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.DateTimeHelper
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.util.Eula
import org.jetbrains.annotations.Contract
import java.util.*


class RegisterAccount : BaseActivity(), View.OnClickListener {
    var email: String? = null
    private var parent: ViewGroup? = null
    private var mImage: ImageView? = null
    private var mEdtEmail: EditText? = null
    private var mEdtPassword: EditText? = null
    private var mEditTextFname: EditText? = null
    private var mEditTextLname: EditText? = null
    private var databaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: AuthStateListener? = null
    private var mLayoutFname: TextInputLayout? = null
    private var mLayoutLname: TextInputLayout? = null
    private val mLayoutName: TextInputLayout? = null
    private var mLayoutEmail: TextInputLayout? = null
    private var mLayoutPassword: TextInputLayout? = null
    private var db: PageDatabase? = null
    private var verified: String = "false"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = PageDatabase(this)
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        mAuth = FirebaseAuth.getInstance()
        verified = Pref(this).get("isFormPage", false).toString()
        init()
    }

    @Contract(pure = true)
    private fun init() {
        mAuthListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                if (verified.equals("true")) {
                    var intent = Intent(this, Form::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.uid)
                    signInPage()
                }
            } else {
                Log.d(TAG, "onAuthStateChanged:signed_out")
                signUpPage()
            }
            if (user != null && user.email != null) email = user.email
        }
        db!!.addPage("id", "pages")
    }

    private fun signUpPage() {
        Eula.show(this@RegisterAccount)
        setContentView(R.layout.activity_signup)
        parent = findViewById(R.id.parent)
        mImage = findViewById(R.id.mImageView)
        mEditTextFname = findViewById(R.id.field_fname)
        mEditTextLname = findViewById(R.id.field_lname)
        mEdtEmail = findViewById(R.id.edt_email)
        mEdtPassword = findViewById(R.id.edt_password)
        mLayoutFname = findViewById(R.id.layout_fname)
        mLayoutLname = findViewById(R.id.layout_lname)
        mLayoutEmail = findViewById(R.id.layout_email)
        mLayoutPassword = findViewById(R.id.layout_password)
        val card: MaterialCardView = findViewById(R.id.v2)
        findViewById<View>(R.id.tvSignIn).setOnClickListener(this)
        findViewById<View>(R.id.email_sign_up_button).setOnClickListener(this)

        Handler().postDelayed({
            val trans: Transition = Slide(Gravity.TOP)
            trans.setDuration(500)
            trans.addTarget(R.id.v2)
            TransitionManager.beginDelayedTransition(parent!!, trans)
            card.visibility = View.VISIBLE
        }, 1)

        //TransitionManager.beginDelayedTransition(container);
        /*var scene1 = Scene.getSceneForLayout(sceneRoot, R.layout.activity_animations_scene1, this)
        TransitionManager.go(scene1, ChangeBounds())*/
    }

    private fun signInPage() {
        Eula.show(this@RegisterAccount)
        setContentView(R.layout.activity_signin)
        parent = findViewById(R.id.parent)
        mImage = findViewById(R.id.mImageView)
        mEdtEmail = findViewById(R.id.edt_email)
        mEdtPassword = findViewById(R.id.edt_password)
        mLayoutFname = findViewById(R.id.layout_fname)
        mLayoutLname = findViewById(R.id.layout_lname)
        mLayoutEmail = findViewById(R.id.layout_email)
        mLayoutPassword = findViewById(R.id.layout_password)
        val card: MaterialCardView = findViewById(R.id.v2)
        findViewById<View>(R.id.tvSignUp).setOnClickListener(this)
        findViewById<View>(R.id.email_sign_in_button).setOnClickListener(this)

        Handler().postDelayed({
            val trans: Transition = Slide(Gravity.TOP)
            trans.setDuration(500)
            trans.addTarget(R.id.v2)
            TransitionManager.beginDelayedTransition(parent!!, trans)
            card.visibility = View.VISIBLE
        }, 1)
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
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tvSignIn -> signInPage()
            R.id.tvSignUp -> signUpPage()
            R.id.email_sign_up_button -> {
                createAccount(mEdtEmail!!.text.toString(), mEdtPassword!!.text.toString())
            }
            R.id.email_sign_in_button -> signIn(mEdtEmail!!.text.toString(), mEdtPassword!!.text.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createAccount(email: String, password: String) {
        if (!validateForm1()) {
            return
        }
        showProgressDialog()
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@RegisterAccount, task.exception!!.message, Toast.LENGTH_SHORT)
                    .show()
            } else {
                val user = mAuth!!.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(mEditTextFname!!.text.toString() + " " + mEditTextLname!!.text.toString()).build()
                user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = mAuth!!.currentUser
                        val reference1 = databaseReference!!.child(firebaseUser!!.uid)
                        //reference1.setValue(new User().setName(firebaseUser.getDisplayName()).setEmail(firebaseUser.getEmail()));
                        reference1.child("Name").setValue(firebaseUser.displayName)
                        reference1.child("Email").setValue(firebaseUser.email)
                        reference1.child("Verified").setValue(firebaseUser.isEmailVerified)
                        reference1.child("Signed Up").setValue(DateTimeHelper().current)
                        firebaseUser.sendEmailVerification().addOnCompleteListener(this@RegisterAccount) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@RegisterAccount,
                                    getString(R.string.verification_email_sent_to) + firebaseUser.email,
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@RegisterAccount,
                                    task.exception!!.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        signInPage()
                    } else {
                        Toast.makeText(
                            this@RegisterAccount,
                            task.exception!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            hideProgressDialog()
        }
    }

    @SuppressLint("LogConditional")
    private fun signIn(email: String, password: String) {
        if (!validateForm2()) {
            return
        }
        showProgressDialog()
        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this
            ) { task ->
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful)
                if (!task.isSuccessful) {
                    toast(this, task.exception!!.message!!)
                } else {
                    var intent = Intent(this, Form::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                    Pref(this).put("isFormPage", true)
                }
                hideProgressDialog()
            }
    }

    private fun validateForm1(): Boolean {
        return when {
            TextUtils.isEmpty(mEditTextFname!!.text.toString()) -> {
                mLayoutFname!!.error = "Required."
                mLayoutFname!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutFname!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutFname!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            TextUtils.isEmpty(mEditTextLname!!.text.toString()) -> {
                mLayoutLname!!.error = "Required."
                mLayoutLname!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutLname!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutLname!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            TextUtils.isEmpty(mEdtEmail!!.text.toString()) -> {
                mLayoutEmail!!.error = "Required."
                mLayoutEmail!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutEmail!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutEmail!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            TextUtils.isEmpty(mEdtPassword!!.text.toString()) -> {
                mLayoutPassword!!.error = "Required."
                mLayoutPassword!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutPassword!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutPassword!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            mEditTextLname!!.text.toString().length <= 3 -> {
                toast(this, "Last Name too short.")
                return false
            }
            mEditTextLname!!.text.toString().length <= 3 -> {
                toast(this, "First Name too short.")
                return false
            }
            mEdtEmail!!.text.toString() == "" || Patterns.EMAIL_ADDRESS.matcher(mEdtEmail!!.text.toString()).matches() -> {
                toast(this, "Please, enter valid email.")
                return false
            }
            mEdtPassword!!.text.toString().length <= 6 -> {
                toast(this, "Password must be greater than 6-digits.")
                return false
            }
            else -> {
                mLayoutFname!!.error = null
                mLayoutLname!!.error = null
                mLayoutEmail!!.error = null
                mLayoutPassword!!.error = null
                true
            }
        }
    }

    private fun validateForm2(): Boolean {
        return when {
            TextUtils.isEmpty(mEdtEmail!!.text.toString()) -> {
                mLayoutEmail!!.error = "Required."
                mLayoutEmail!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutEmail!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutEmail!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            TextUtils.isEmpty(mEdtPassword!!.text.toString()) -> {
                mLayoutPassword!!.error = "Required."
                mLayoutPassword!!.setErrorTextColor(ColorStateList.valueOf(Color.YELLOW))
                mLayoutPassword!!.setErrorIconTintList(ColorStateList.valueOf(Color.YELLOW))
                mLayoutPassword!!.boxStrokeErrorColor = ColorStateList.valueOf(Color.YELLOW)
                false
            }
            else -> {
                mLayoutEmail!!.error = null
                mLayoutPassword!!.error = null
                true
            }
        }
    }

    /* @SuppressLint("StaticFieldLeak")
   private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
       @Override
       protected Bitmap doInBackground(String... urls) {
           Bitmap mIcon = null;
           try {
               InputStream in = new URL(urls[0]).openStream();
               mIcon = BitmapFactory.decodeStream(in);
           } catch (Exception e) {
               e.printStackTrace();
           }
           return mIcon;
       }

       @Override
       protected void onPostExecute(Bitmap result) {
           if (result != null) {
               mImageView.getLayoutParams().width = (getResources().getDisplayMetrics().widthPixels / 100) * 24;
               mImageView.setImageBitmap(result);
           }
       }
   }*/

    companion object {
        private const val TAG = "RegisterAccount"
    }
}