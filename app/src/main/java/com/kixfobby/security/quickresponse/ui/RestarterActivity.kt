package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.kixfobby.security.crash.CustomActivityOnCrash
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.ClipBoardManager

class RestarterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_error)

        val image = findViewById<ImageView>(R.id.image)
        val errorDetailsText = findViewById<TextView>(R.id.error_details)
        errorDetailsText.text = CustomActivityOnCrash.getStackTraceFromIntent(intent)
        val restartButton = findViewById<Button>(R.id.restart_button)
        val config = CustomActivityOnCrash.getConfigFromIntent(intent)
        if (config == null) {
            //This should never happen - Just finish the activity to avoid a recursive crash.
            finish()
            return
        }
        if (config.isShowRestartButton && config.restartActivityClass != null) {
            restartButton.setText(R.string.restart_app)
            restartButton.setOnClickListener {
                CustomActivityOnCrash.restartApplication(
                    this@RestarterActivity,
                    config
                )
            }
        } else {
            restartButton.setOnClickListener {
                CustomActivityOnCrash.closeApplication(
                    this@RestarterActivity,
                    config
                )
            }
        }

        image.setOnClickListener {
            ClipBoardManager().copyToClipboard(this, errorDetailsText.text.toString())
            toast(this, "Error copied to clipboard!")
        }
    }
}