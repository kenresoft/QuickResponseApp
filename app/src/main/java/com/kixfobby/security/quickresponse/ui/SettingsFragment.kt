package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kixfobby.security.quickresponse.R

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private const val KEY_PREFERENCE_LANGUAGE = "language"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        setHasOptionsMenu(false)
        findPreference<Preference>(KEY_PREFERENCE_LANGUAGE)?.setOnPreferenceChangeListener { _, newValue ->
            val language = newValue.toString()
            (activity as PreferenceChangeListener).onLanguagePreferenceChanged(language)
            true
        }

        /*  // Set intent for contact notification option
          val contactScreen: PreferenceScreen? = findPreference(getString(R.string.contacts_key))
          contactScreen!!.intent = Intent(context, ConfigContactsActivity::class.java)
  */
        // Set intent for quick message option
        /*val messageScreen: PreferenceScreen? = findPreference(getString(R.string.quickmessages_key))
        messageScreen!!.intent = Intent(context, ConfigQuickMessagesActivity::class.java)*/

        /*val messageScreen: PreferenceScreen? = findPreference("about")
        messageScreen!!.intent = Intent(context, StartActivity::class.java)*/
    }

    interface PreferenceChangeListener {
        fun onLanguagePreferenceChanged(langauge: String)
    }
}
