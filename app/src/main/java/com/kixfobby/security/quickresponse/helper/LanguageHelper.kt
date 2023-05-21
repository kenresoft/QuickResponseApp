package com.kixfobby.security.quickresponse.helper

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Pref
import java.util.*

class LanguageHelper : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setLanguage("en")
        val ctyKey = Pref(this).get("iso", "ctryKey")?.trim { it <= ' ' }
        if (ctyKey != null) {
            //String cty = CountryHelper.getCode().get(ctyKey);
            val ct = Locale("en", ctyKey).displayCountry
            Pref(this).put("ct", ct)
            startActivity(Intent(this, Resolver::class.java))
            finish()
        } else Toast.makeText(this, "Error encountered, setting account!", Toast.LENGTH_SHORT).show()
    }
}