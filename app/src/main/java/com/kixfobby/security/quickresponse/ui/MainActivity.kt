package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.kixfobby.security.floaty.UI.FloatingActivity
import com.kixfobby.security.floaty.UI.FloatingMenuDialog
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Constants
import com.kixfobby.security.quickresponse.storage.PageDatabase
import com.kixfobby.security.quickresponse.ui.Dashboard

class MainActivity : BaseActivity() {
    private var db: PageDatabase? = null
    private var c: Cursor? = null
    private var row = false
    private var id: String? = null
    private var page: String? = null
    private var parent_view: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        db = PageDatabase(this)
        c = db!!.allPages
        row = c!!.moveToLast()
        if (row) {
            id = c!!.getString(0)
            page = c!!.getString(1)
        }
        db!!.updatePage(Constants.HOME)
        parent_view = findViewById(android.R.id.content)
        (findViewById<View>(R.id.sign_in_for_account)).setOnClickListener {
            //Snackbar.make(parent_view!!, "Sign up for an account", Snackbar.LENGTH_SHORT).show()
            FloatingMenuDialog(this)
                .setDialogTitle("Add Picture")
                .setPositveButtonText("From Camera")
                .setNeutralButtonText("From Gallery")
                .setExtraButtonText("From Google Drive")
                .setNegativeButtonText("Close Dialog")
                .setDismissDialogOnMenuOnClick(false)
                .setDialogCancelable(true)

                .setPositiveTextColor("#FF86BF71")
                .setNeutralTextColor(R.color.colorAccent)
                .setNegativeTextColor("#FF86BF71")
                .setExtraTextColor("#FFFFE640")
                .setTitleTextColor("#FEEFE640")
                .setFontPath("GothamRnd-Bold.otf")

                .setOnPositiveButtonOnClick {
                    Toast.makeText(this, "Positive", Toast.LENGTH_SHORT).show()
                }
                .setOnNegativeButtonOnClick {
                    Toast.makeText(this, "Negative", Toast.LENGTH_SHORT).show();
                }
                .setOnNeutralButtonOnClick {
                    Toast.makeText(this, "Neutral", Toast.LENGTH_SHORT).show();
                }
                .setOnExtraButtonOnClick {
                    Toast.makeText(this, "Extra", Toast.LENGTH_SHORT).show();
                }
                .show();
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_page, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dashboard -> {
                startActivity(Intent(this@MainActivity, Travel::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}