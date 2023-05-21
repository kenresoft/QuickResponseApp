package com.kixfobby.security.quickresponse.helper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.home.Self

class Resolver : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val lanKey = Pref(this).get("language", "langKey")
        setLanguage(lanKey!!)
        startActivity(Intent(this, Self::class.java))
        finish()
    }

    companion object {
        fun getLangKey(activity: Activity, input: String): String {
            val output: String
            output = if (activity.getString(R.string.lang_default) == input) {
                "en"
            } else if (activity.getString(R.string.lang_chinese) == input) {
                "zh"
            } else if (activity.getString(R.string.lang_english) == input) {
                "en"
            } else if (activity.getString(R.string.lang_french) == input) {
                "fr"
            } else if (activity.getString(R.string.lang_italian) == input) {
                "it"
            } else if (activity.getString(R.string.lang_spanish) == input) {
                "es"
            } else "en"
            return output
        }

        fun getLang(activity: Activity, input: String): String {
            val output: String
            output = when (input) {
                "zh" -> activity.getString(R.string.lang_chinese)
                "en" -> activity.getString(R.string.lang_english)
                "fr" -> activity.getString(R.string.lang_french)
                "it" -> activity.getString(R.string.lang_italian)
                "es" -> activity.getString(R.string.lang_spanish)
                else -> activity.getString(R.string.lang_english)
            }
            return output
        }
    }
}