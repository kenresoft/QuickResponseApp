package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R

class SettingsActivity : BaseActivity(), SettingsFragment.PreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar!!.setTitle(R.string.settings)
        supportFragmentManager.beginTransaction().replace(R.id.layoutFragmentContainer, SettingsFragment()).commit()
    }

    override fun onLanguagePreferenceChanged(langauge: String) {
        setLanguage(langauge)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
