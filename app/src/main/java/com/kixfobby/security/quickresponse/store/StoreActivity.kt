package com.kixfobby.security.quickresponse.store

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingManager
import com.kixfobby.security.quickresponse.databinding.ActivityStoreBinding
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import com.kixfobby.security.quickresponse.ui.RegisterAccount
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

/**
 * StoreActivity, which displays inApp and subscription products as a list and each products has its
 * own name and price and users buy them by tapping buy button. Purchases made from Google Play
 * Billing Library.
 */

@AndroidEntryPoint
class StoreActivity : BaseActivity() {
    private val skuProductsAndPurchasesList: MutableList<BillingSkuRelatedPurchases?> =
        ArrayList<BillingSkuRelatedPurchases?>()

    @set:Inject
    var billingManager: BillingManager? = null
    var storeBinding: ActivityStoreBinding? = null
    var storeAdapter: StoreAdapter? = null

    /**
     * Observes changes and updates of Sku Products and Purchases which is stored in local database.
     * Updates observed changes to the products list.
     */
    private val skuProductsAndPurchasesObserver: Observer<List<BillingSkuRelatedPurchases?>?> =
        Observer<List<BillingSkuRelatedPurchases?>?> { skuRelatedPurchasesList: List<BillingSkuRelatedPurchases?>? ->
            if (skuRelatedPurchasesList != null && skuRelatedPurchasesList.isNotEmpty()) {
                skuProductsAndPurchasesList.clear()
                skuProductsAndPurchasesList.addAll(skuRelatedPurchasesList)
                storeAdapter?.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storeBinding = DataBindingUtil.setContentView(this, R.layout.activity_store)
        init()
        if (FirebaseAuth.getInstance().currentUser != null) {
            checkPaidUser()
        } else {
            startActivity(Intent(baseContext, RegisterAccount::class.java))
        }
    }

    /**
     * Sets Toolbar. Initializes Presenter ProductsAndPurchasesViewModel, BillingViewModel and
     * registers LifeCycle Observers. Observes Sku Products and Purchases. Initializes RecyclerView
     * Products List and its adapter.
     */
    private fun init() {
        val storeVM: StoreVM = ViewModelProvider(this).get<StoreVM>(StoreVM::class.java)
        this.lifecycle.addObserver(storeVM)
        storeAdapter = StoreAdapter(this, skuProductsAndPurchasesList, billingManager!!)
        storeBinding!!.rvStore.adapter = storeAdapter
        storeVM.skuProductsAndPurchasesList.observe(this, skuProductsAndPurchasesObserver)
    }

    companion object {
        /**
         * Launches StoreActivity.
         *
         * @param context An Activity Context.
         */
        fun start(context: Context) {
            if (context is AppCompatActivity) {
                val intent = Intent(context, StoreActivity::class.java)
                context.startActivity(intent)
                startActivityAnimation(context)
            }
        }
    }
}