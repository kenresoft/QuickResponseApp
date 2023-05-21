package com.kixfobby.security.quickresponse.store

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.android.billingclient.api.SkuDetails
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingConstants
import com.kixfobby.security.quickresponse.billing.BillingManager
import com.kixfobby.security.quickresponse.room.entity.BillingPurchaseDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.util.DateTimeUtil
import org.json.JSONException

/**
 * StoreItemVM, a Presenter which provides Store Product Item that has product sku details, name,
 * price and its purchase details.
 *
 *
 * Handles Click Event Actions from View and performs Purchase Flow.
 */
class StoreItemVM(
    private val context: Context,
    private val billingManager: BillingManager,
    productRelatedPurchases: BillingSkuRelatedPurchases
) {
    @JvmField
    val skuProductName = ObservableField<String>()

    @JvmField
    val skuProductPrice = ObservableField<String>()

    @JvmField
    val isAlreadyPurchased = ObservableField<Boolean>()
    private val productPurchaseDetails: List<BillingPurchaseDetails> = productRelatedPurchases.billingPurchaseDetails
    private val skuProductDetails: BillingSkuDetails = productRelatedPurchases.billingSkuDetails

    /**
     * Initializes Store Product Item and sets its respective values and tells the updates to the
     * view.
     */

    private fun init() {
        skuProductPrice.set(skuProductDetails.skuPrice)
        if (skuProductDetails.skuID == BillingConstants.SKU_BUY_APPLE) {
            // This is Apple.
            skuProductName.set(context.getString(R.string.app_name))
            // For apple, it can be bought multiple times.
            isAlreadyPurchased.set(java.lang.Boolean.FALSE)
        } /*else {
            // This is Popcorn.
            skuProductName.set(context.getString(R.string.unlimited_popcorn))
            checkPopcornPurchaseStatus()
        }*/
    }

    /**
     * Checks whether the Popcorn Store Product Item was purchased or not and it tells the updates
     * to the view.
     *
     *
     * "Buy" option will be available if it was not purchased. "Purchased" will be shown
     * otherwise.
     */
    private fun checkPopcornPurchaseStatus() {
        // Unlimited popcorn was not purchased yet.
        if (productPurchaseDetails.isEmpty()) {
            isAlreadyPurchased.set(java.lang.Boolean.FALSE)
            return
        }
        var productPurchaseTimeInMillis = productPurchaseDetails[productPurchaseDetails.size - 1].purchaseTime
        // Test Subscriptions are valid for 5 minutes from the purchase time. For
        // production release, add 30 days to the purchase time that gives the expiry date of
        // subscription
        productPurchaseTimeInMillis += DateTimeUtil.FIVE_MINUTES_IN_MILLIS
        // Unlimited popcorn purchase was expired if true, and need to buy again.
        if (DateTimeUtil.isDateTimePast(productPurchaseTimeInMillis)) {
            isAlreadyPurchased.set(java.lang.Boolean.FALSE)
            return
        }
        // Popcorn was already purchased, no need to buy again.
        isAlreadyPurchased.set(java.lang.Boolean.TRUE)
    }

    /**
     * Handles Click Events from View.
     *
     * @param v A view in which the click action performed.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun onClick(v: View) {
        if (v.id == R.id.btn_product_buy) {
            var isPaymentMade = Pref(context).get("isPaymentMade", defValue = false)
            if (isPaymentMade) {
                Toast.makeText(
                    context,
                    "A previous payment is yet to be linked to a user. Try to complete the payment process before initiating a new one.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                initPurchaseFlow()
            }
        }
    }

    /** Performs Purchase Flow through BillingClient of Google Play Billing Library.  */
    @RequiresApi(Build.VERSION_CODES.N)
    fun initPurchaseFlow() {
        try {
            val skuDetails = SkuDetails(skuProductDetails.originalJson)
            billingManager.initiatePurchaseFlow((context as Activity), skuDetails)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("storeItemSrcCompat")
        fun setStoreItemSrcCompat(
            iv: ImageView, skuProductName: String
        ) {
            val drawableRes =
                if (skuProductName == iv.context.getString(R.string.app_name)) R.drawable.qr_logo360 else R.drawable.baseline_payment_24
            iv.setImageResource(drawableRes)
        }
    }

    /**
     * Initializes Store Product Item attributes.
     *
     * @param context An AppCompatActivity Context.
     * @param billingManager Provides access to BillingClient which perform Product Purchases from
     * Google Play Billing Library.
     * @param productRelatedPurchases contains Products with its Sku Details and its related
     * Purchases.
     */
    init {
        init()
    }
}