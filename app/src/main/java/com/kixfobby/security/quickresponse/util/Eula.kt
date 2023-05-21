package com.kixfobby.security.quickresponse.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.kixfobby.security.quickresponse.KixfActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.Pref
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * callback to let the activity know when the user has accepted the EULA.
 */
internal interface OnEulaAgreedTo {
    /**
     * Called when the user has accepted the eula and the dialog closes.
     */
    fun onEulaAgreedTo()
}

/**
 * callback to let the activity know when the user has not accepted the EULA.
 */
internal interface OnEulaNotAgreedTo {
    /**
     * Called when the user has not accepted the eula and the dialog closes.
     */
    fun onEulaNotAgreedTo()
}

/**
 * Displays an EULA ("End User License Agreement") that the user has to accept before using the
 * application. Your application should call [Eula.show] in the
 * onCreate() method of the first activity. If the user accepts the EULA, it will never be shown
 * again. If the user refuses, [android.app.Activity.finish] is invoked on your activity.
 */
object Eula {
    private val ASSET_EULA = "eula.txt"
    private val PREFERENCE_EULA_ACCEPTED = "eula.accepted"
    private val PREFERENCES_EULA = "eula"
    private var dialog: Dialog? = null
    private var lp: WindowManager.LayoutParams? = null
    private var webview: WebView? = null

    /**
     * Displays the EULA if necessary. This method should be called from the onCreate() method of
     * your main Activity.
     *
     * @param activity The Activity to finish if the user rejects the EULA.
     * @return Whether the user has agreed already.
     */
    fun show(context: Context) {
        var activity = KixfActivity().getActivity(context)
        var preferences: Boolean = Pref(context).get(PREFERENCE_EULA_ACCEPTED, false)

        if (preferences.toString() == "false") {
            dialog = Dialog(activity!!, R.style.CustomDialogTheme)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE) // before
            dialog?.setContentView(R.layout.item_dialog)
            dialog?.setCancelable(false)
            dialog?.findViewById<View>(R.id.webview)!!.isClickable = false
            dialog?.findViewById<View>(R.id.webview)!!.isLongClickable = false
            dialog?.findViewById<View>(R.id.webview)!!.setOnClickListener(null)
            dialog?.findViewById<View>(R.id.webview)!!.setOnLongClickListener(null)
            webview = dialog?.findViewById<View>(R.id.webview) as WebView
            webview?.settings?.setRenderPriority(WebSettings.RenderPriority.HIGH)
            (dialog!!.findViewById<View>(R.id.title) as TextView).text =
                activity.getString(R.string.app_term_condition)
            lp = WindowManager.LayoutParams()
            lp?.copyFrom(dialog?.window!!.attributes)
            lp?.width = WindowManager.LayoutParams.MATCH_PARENT
            lp?.height = WindowManager.LayoutParams.MATCH_PARENT
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            dialog!!.findViewById<View>(R.id.bt_close).setOnClickListener(View.OnClickListener {
                refuse(activity)
                dialog!!.dismiss()
            })
            dialog!!.findViewById<MaterialButton>(R.id.bt_accept).setOnClickListener {
                Toast.makeText(activity, "Thanks for your feedback", Toast.LENGTH_SHORT).show()
                accept(context)
                dialog!!.dismiss()
                //KixfActivity().grantPermissions(context)
            }
            renderWebPage(Constants.TERM)
            if (!KixfActivity().getActivity(dialog?.context)?.isFinishing!!) {
                dialog?.show()
                dialog?.window!!.attributes = lp
            }
        }
    }

    fun renderWebPage(urlToRender: String?) {
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
        webview?.loadUrl(urlToRender!!)
    }


    private fun accept(context: Context) {
        Pref(context).put(PREFERENCE_EULA_ACCEPTED, true)
    }

    private fun refuse(activity: Activity) {
        val packageUri = Uri.parse("package:com.kixfobby.security.quickresponse")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        activity.startActivity(uninstallIntent)
        activity.finish()
    }

    private fun readEula(activity: Activity): CharSequence {
        var `in`: BufferedReader? = null
        try {
            `in` = BufferedReader(InputStreamReader(activity.assets.open(ASSET_EULA)))
            var line: String?
            val buffer = StringBuilder()
            while ((`in`.readLine().also { line = it }) != null) buffer.append(line).append('\n')
            return buffer
        } catch (e: IOException) {
            return ""
        } finally {
            closeStream(`in`)
        }
    }

    /**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private fun closeStream(stream: Closeable?) {
        if (stream != null) {
            try {
                stream.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }

}