package com.kixfobby.security.quickresponse

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings.RenderPriority
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.CameraController
import androidx.core.content.FileProvider
import com.androidhiddencamera.HiddenCameraFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.kixfobby.security.quickresponse.helper.UpdateDialog
import com.kixfobby.security.quickresponse.service.DemoCamService
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.Pref
import com.tapadoo.alerter.Alerter
import java.io.File

@SuppressLint("Registered")
open class BaseActivity : KixfActivity() {

    @JvmField
    var noBackPress = false
    var appUpdateDialog: UpdateDialog? = null
    private var mProgressDialog: ProgressDialog? = null
    private var dialog: Dialog? = null
    private var dialog2: Dialog? = null
    private var mDialog: Dialog? = null
    private val mIndicator: Dialog? = null
    private var lp: WindowManager.LayoutParams? = null
    private var webview: WebView? = null
    private val progress: ProgressDialog? = null
    private var mHiddenCameraFragment: HiddenCameraFragment? = null


    fun transite(): Bundle? {
        val opt: ActivityOptions? = ActivityOptions.makeSceneTransitionAnimation(this)
        return opt?.toBundle()
    }

    fun animate() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    protected fun callGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(intent, "Choose a profile picture!"), GALLERY_PICTURE)
    }

    private fun hasCamera(): Boolean {
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        ) {
            true
        } else {
            false
        }
    }

    fun openCamera() {
        if (hasCamera()) {

            val file: String = "qr_alert_shot_" + System.currentTimeMillis().toString() + ".jpg"
            val mediaFile = File(Companion.storageLocation + file)
            Pref(baseContext).put("file", file)
            Pref(baseContext).put("imageFile", mediaFile.absolutePath)

            val imageUri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", mediaFile)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(intent, CameraController.IMAGE_CAPTURE)
        }
    }

    fun openVideo() {
        if (hasCamera()) {
            val file: String = "qr_alert_shot_" + System.currentTimeMillis().toString() + ".mp4"
            val mediaFile = File(Companion.storageLocation + file)
            Pref(baseContext).put("file", file)

            val videoUri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", mediaFile)
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            startActivityForResult(intent, CameraController.VIDEO_CAPTURE)
        }
    }

    fun captureBackground() {
        if (mHiddenCameraFragment != null) {    //Remove fragment from container if present
            getSupportFragmentManager()
                .beginTransaction()
                .remove(mHiddenCameraFragment!!)
                .commit()
            mHiddenCameraFragment = null
        }
        startService(Intent(this, DemoCamService::class.java))
        animate()
    }

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this, R.style.CustomDialogTheme)
            mProgressDialog?.setMessage(getString(R.string.loading))
            mProgressDialog?.isIndeterminate = true
        }
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog?.isShowing!!) {
            mProgressDialog?.dismiss()
        }
    }

    fun showNetworkDialog() {
        if (mDialog == null) {
            mDialog = Dialog(this, R.style.NetworkDialogTheme)
            mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            mDialog?.setContentView(R.layout.dialog_network)
            mDialog?.setCancelable(false)
            mDialog?.findViewById<View>(R.id.bt_close)!!.setOnClickListener {
                mDialog?.dismiss()
                //Toast.makeText(this@BaseActivity, R.string.click_on_location_display_to_refresh, Toast.LENGTH_LONG).show()
            }
        }
        if (!getActivity(mDialog?.context)?.isFinishing!!) {
            mDialog?.show()
        }
    }

    fun hideNetworkDialog() {
        if (mDialog != null && mDialog?.isShowing!!) {
            mDialog?.dismiss()
        }
    }

    fun showPrivacyDialog() {
        if (dialog == null) {
            dialog = Dialog(this, R.style.CustomDialogTheme)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog?.setContentView(R.layout.item_dialog)
            dialog?.setCancelable(false)
            dialog?.findViewById<View>(R.id.webview)!!.isClickable = false
            dialog?.findViewById<View>(R.id.webview)!!.isLongClickable = false
            dialog?.findViewById<View>(R.id.webview)!!.setOnClickListener(null)
            dialog?.findViewById<View>(R.id.webview)!!.setOnLongClickListener(null)
            webview = dialog?.findViewById<View>(R.id.webview) as WebView
            webview?.settings?.setRenderPriority(RenderPriority.HIGH)
            (dialog?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.app_privacy_policy)
            lp = WindowManager.LayoutParams()
            lp?.copyFrom(dialog?.window!!.attributes)
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT
            lp?.height = WindowManager.LayoutParams.MATCH_PARENT
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            dialog?.findViewById<View>(R.id.bt_close)!!.setOnClickListener { dialog?.dismiss() }
            (dialog?.findViewById<View>(R.id.bt_accept) as MaterialButton).text = getString(R.string.okay)
            dialog?.findViewById<View>(R.id.bt_accept)?.setOnClickListener { dialog?.dismiss() }
            renderPrivacyWebPage(Constants.POLICY)
        }
        if (!getActivity(dialog?.context)?.isFinishing!!) {
            dialog?.show()
            dialog?.window!!.attributes = lp
        }
    }

    protected fun renderPrivacyWebPage(url: String?) {
        webview?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return true
            }
        }
        webview?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {}
        }
        webview?.settings!!.javaScriptEnabled = true
        webview?.setOnLongClickListener { true }
        webview?.isLongClickable = false
        webview?.isHapticFeedbackEnabled = false
        webview?.loadUrl(url!!)
    }

    fun showTermsDialog() {
        if (dialog2 == null) {
            dialog2 = Dialog(this, R.style.CustomDialogTheme)
            dialog2?.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog2?.setContentView(R.layout.item_dialog)
            dialog2?.setCancelable(false)
            dialog2?.findViewById<View>(R.id.webview)!!.isClickable = false
            dialog2?.findViewById<View>(R.id.webview)!!.isLongClickable = false
            dialog2?.findViewById<View>(R.id.webview)!!.setOnClickListener(null)
            dialog2?.findViewById<View>(R.id.webview)!!.setOnLongClickListener(null)
            webview = dialog2?.findViewById<View>(R.id.webview) as WebView
            webview?.settings?.setRenderPriority(RenderPriority.HIGH)
            (dialog2?.findViewById<View>(R.id.title) as TextView).text = getString(R.string.app_term_condition)
            lp = WindowManager.LayoutParams()
            lp?.copyFrom(dialog2?.window!!.attributes)
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT
            lp?.height = WindowManager.LayoutParams.MATCH_PARENT
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            dialog2?.findViewById<View>(R.id.bt_close)!!.setOnClickListener { dialog2?.dismiss() }
            (dialog2?.findViewById<View>(R.id.bt_accept) as MaterialButton).text = getString(R.string.okay)
            dialog2?.findViewById<View>(R.id.bt_accept)?.setOnClickListener { dialog2?.dismiss() }
            renderTermsWebPage(Constants.TERM)
        }
        if (!getActivity(dialog2?.context)?.isFinishing!!) {
            dialog2?.show()
            dialog2?.window!!.attributes = lp
        }
    }

    protected fun renderTermsWebPage(url: String?) {
        webview?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return true
            }
        }
        webview?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {}
        }
        webview?.settings!!.javaScriptEnabled = true
        webview?.setOnLongClickListener { true }
        webview?.isLongClickable = false
        webview?.isHapticFeedbackEnabled = false
        webview?.loadUrl(url!!)
    }

    /*fun showNotifAlert() {
        val notifViewCount = PreferenceManager.getDefaultSharedPreferences(this).getInt("nvCount", 0)
        if (notifViewCount < 3) {
            Alerter.create(this)
                .setTitle("New message")
                .setText("")
                .setDuration(7000)
                .setBackgroundColorRes(R.color.colorPrimaryLight)
                .setIcon(R.drawable.ic_launcher_foreground) //.enableProgress(true)
                .setProgressColorRes(R.color.colorAccent)
                .setOnClickListener {
                    PreferenceManager.getDefaultSharedPreferences(this@BaseActivity).edit().putInt("nvCount", 4).apply()
                }
                .show()
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("nvCount", notifViewCount + 1).apply()
        }
    }*/

    fun showAlert(message: String?) {
        Alerter.create(this)
            .setTitle("SECURITY ALERT SUCCESSFUL!")
            .setText(message!!)
            .setDuration(7000)
            .setBackgroundColorRes(R.color.colorPrimaryLight)
            .setIcon(R.drawable.ic_action_edit_message) //.enableProgress(true)
            .setProgressColorRes(R.color.colorAccent)
            .setOnClickListener {
                //TODO;
            }
            .show()
    }

    fun hideTermsDialog() {
        if (mDialog != null && mDialog?.isShowing!!) {
            mDialog?.dismiss()
        }
    }

    fun showSnackbar(view: View, message: String): Snackbar? {

        val snackbar: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        // 15 is margin from all the sides for snackbar
        val marginFromSides = 15
        val height = 100f

        //inflate view
        val snackView: View = layoutInflater.inflate(R.layout.custom_toast_layout, null)

        // White background
        snackbar.getView().setBackgroundColor(Color.WHITE)
        // for rounded edges
        snackbar.getView().setBackground(resources.getDrawable(R.drawable.background))
        val snackBarView: Snackbar.SnackbarLayout = snackbar.getView() as Snackbar.SnackbarLayout
        val parentParams = snackBarView.getLayoutParams()
        //parentParams.setMargins(marginFromSides, 0, marginFromSides, marginFromSides)
        parentParams.height = height.toInt()
        parentParams.width = FrameLayout.LayoutParams.MATCH_PARENT
        snackBarView.setLayoutParams(parentParams)
        snackBarView.addView(snackView, 0)
        return snackbar
    }

    fun refresh() {
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onStop() {
        super.onStop()
        hideProgressDialog()
        hideNetworkDialog()
        hideTermsDialog()
    }

    override fun onBackPressed() {
        if (noBackPress == true) {
            Log.w("onBackPressed", "disabled!")
        } else {
            super.onBackPressed()
            animate()
        }
    }

    companion object {
        const val GALLERY_PICTURE = 1
        const val IMAGE_CAPTURE = 101
        const val VIDEO_CAPTURE = 102

        fun startActivityAnimation(context: Context) {
            if (context is AppCompatActivity) {
                context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }

        fun printLog(tag: String, message: String) {
            Log.d(tag, message)
        }

        fun showSnackBar(v: View, @StringRes stringResID: Int) {
            Snackbar.make(v, stringResID, Snackbar.LENGTH_LONG).show()
        }

        fun showSnackBar(v: View, string: String?) {
            Snackbar.make(v, string!!, Snackbar.LENGTH_LONG).show()
        }

        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        public val storageLocation: String = Environment.getExternalStorageDirectory().absolutePath + "/.QuickResponse/"

    }
}