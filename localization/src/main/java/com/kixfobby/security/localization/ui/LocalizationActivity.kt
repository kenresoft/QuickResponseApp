package com.kixfobby.security.localization.ui

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kixfobby.security.localization.core.LocalizationActivityDelegate
import com.kixfobby.security.localization.core.OnLocaleChangedListener
import java.util.*

abstract class LocalizationActivity : AppCompatActivity(), OnLocaleChangedListener {
    private val localizationDelegate: LocalizationActivityDelegate by lazy { LocalizationActivityDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        localizationDelegate.onResume(this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localizationDelegate.attachBaseContext(newBase))
    }

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return localizationDelegate.getResources(super.getResources())
    }

    override fun onBeforeLocaleChanged() {
        // Just override method locale change event
    }

    override fun onAfterLocaleChanged() {
        // Just override method locale change event
    }

    fun setLanguage(language: String) {
        localizationDelegate.setLanguage(this, language)
    }

    fun setLanguage(language: String, country: String) {
        localizationDelegate.setLanguage(this, language, country)
    }

    fun setLanguage(locale: Locale) {
        localizationDelegate.setLanguage(this, locale)
    }

    fun getCurrentLanguage(): Locale {
        return localizationDelegate.getLanguage(this)
    }
}
