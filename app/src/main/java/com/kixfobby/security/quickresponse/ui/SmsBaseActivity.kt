package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.fragment.ChatFragment
import com.kixfobby.security.quickresponse.fragment.SmsFragment
import com.kixfobby.security.spacetablayout.SpaceTabLayout
import java.util.*

class SmsBaseActivity : BaseActivity() {
    var tabLayout: SpaceTabLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.setTitle(R.string.manage_sms)

        val fragmentList: MutableList<Fragment> = ArrayList()
        fragmentList.add(SmsFragment())
        fragmentList.add(ChatFragment())

        findViewById<View>(R.id.coordinator_main) as CoordinatorLayout
        val viewPager = findViewById<View>(R.id.viewPager) as ViewPager
        tabLayout = findViewById<View>(R.id.spaceTabLayout) as SpaceTabLayout

        tabLayout!!.initialize(viewPager, supportFragmentManager, fragmentList)
        tabLayout!!.setTabOneIcon(R.drawable.ic_textsms)
        tabLayout!!.setTabTwoIcon(R.drawable.ic_chat)
        tabLayout!!.tabTwoOnClickListener = View.OnClickListener { }
        tabLayout!!.setOnClickListener { }
    }
}