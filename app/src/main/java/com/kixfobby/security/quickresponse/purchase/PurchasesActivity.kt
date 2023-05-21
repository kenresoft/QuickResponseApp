package com.kixfobby.security.quickresponse.purchase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.ActivityViewPurchasesBinding
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import com.kixfobby.security.quickresponse.store.StoreActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * PurchasesActivity, which displays list of inApp and subscription products that are all purchased
 * by the user.
 */
@AndroidEntryPoint
class PurchasesActivity : BaseActivity() {
    private val skuProductsAndPurchasesList: MutableList<BillingSkuRelatedPurchases?> = ArrayList<BillingSkuRelatedPurchases?>()
    private var viewPurchasesBinding: ActivityViewPurchasesBinding? = null
    private var purchasesAdapter: PurchasesAdapter? = null

    /**
     * Observes changes and updates of Sku Products and Purchases which is stored in local database.
     * Updates observed changes to the products list.
     */
    private val skuProductsAndPurchasesObserver: Observer<List<BillingSkuRelatedPurchases?>?> =
        Observer<List<BillingSkuRelatedPurchases?>?> { skuRelatedPurchasesList: List<BillingSkuRelatedPurchases?>? ->
            if (skuRelatedPurchasesList != null && skuRelatedPurchasesList.isNotEmpty()) {
                skuProductsAndPurchasesList.clear()
                skuProductsAndPurchasesList.addAll(skuRelatedPurchasesList)
                purchasesAdapter!!.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPurchasesBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_purchases)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "Make Payment").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_payment_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                startActivity(Intent(baseContext, StoreActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Sets Toolbar. Initializes Presenter ProductsAndPurchasesViewModel and registers LifeCycle
     * Observers. Observes Sku Products and Purchases. Initializes RecyclerView Products List and
     * its adapter.
     */
    private fun init() {
        /*setToolbar(
                viewPurchasesBinding.tbWidget.toolbar,
                true,
                getString(R.string.your_purchases),
                viewPurchasesBinding.tbWidget.tvToolbarTitle);*/
        val purchasesVM: PurchasesVM = ViewModelProvider(this).get<PurchasesVM>(PurchasesVM::class.java)
        this.lifecycle.addObserver(purchasesVM)
        purchasesAdapter = PurchasesAdapter(this, skuProductsAndPurchasesList)
        viewPurchasesBinding!!.rvProductsPurchases.adapter = purchasesAdapter
        purchasesVM.skuProductsAndPurchasesList.observe(this, skuProductsAndPurchasesObserver)
    }

    companion object {
        /**
         * Launches PurchasesActivity.
         *
         * @param context An Activity Context.
         */
        fun start(context: Context) {
            if (context is AppCompatActivity) {
                val intent = Intent(context, PurchasesActivity::class.java)
                context.startActivity(intent)
                startActivityAnimation(context)
            }
        }
    }
}