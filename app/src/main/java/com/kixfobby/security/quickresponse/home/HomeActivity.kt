package com.kixfobby.security.quickresponse.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * HomeActivity, users can view their purchases and buy products from store by tapping respective
 * buttons on the ViewGroup.
 */

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private var homeBinding: ActivityHomeBinding? = null

    /**
     * A Premium Purchase Observer, observes about whether the Premium Purchase has been already
     * purchased by user or not. If it was purchased, user has granted access for accessing View
     * Your Purchases and Buy From Store. These two features are locked otherwise, and the user
     * needs to purchase this in order to use those features.
     */
    private val isPremiumPurchasedObserver = Observer { value: Boolean? ->
        val isPurchased = value ?: false
        // Dismisses BillingPremiumDialog after successful purchase of Premium Feature.
        if (isPurchased) {
            BillingPremiumDialog.dismiss(this@HomeActivity)
        }
        HomeVM.setDrawableRight(homeBinding!!.btnBuyFromStore, isPurchased)
        HomeVM.setDrawableEnd(homeBinding!!.btnBuyFromStore, isPurchased)
        HomeVM.setDrawableRight(homeBinding!!.btnViewYourPurchases, isPurchased)
        HomeVM.setDrawableEnd(homeBinding!!.btnViewYourPurchases, isPurchased)
        homeBinding!!.executePendingBindings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeBinding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        init()
    }

    /**
     * Sets Toolbar. Initializes Presenter HomeViewModel, BillingViewModel and registers LifeCycle
     * Observers. Observes Premium Purchase.
     */
    private fun init() {
        /*setToolbar(
                homeBinding.tbWidget.toolbar,
                false,
                getString(R.string.home),
                homeBinding.tbWidget.tvToolbarTitle);*/
        val homeVM = ViewModelProvider(this).get(HomeVM::class.java)
        homeBinding!!.presenter = homeVM
        this.lifecycle.addObserver(homeVM)
        homeVM.isPremiumPurchased.observe(this, isPremiumPurchasedObserver)
    }

    companion object {
        /**
         * Launches HomeActivity.
         *
         * @param context An Activity Context.
         */
        @JvmStatic
        fun start(context: Context) {
            if (context is AppCompatActivity) {
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
                startActivityAnimation(context)
            }
        }
    }
}