package com.kixfobby.security.quickresponse.purchase

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingConstants
import com.kixfobby.security.quickresponse.room.entity.BillingPurchaseDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import com.kixfobby.security.quickresponse.util.DateTimeUtil

/**
 * PurchaseItemVM, a Presenter which provides Product Item that has product sku details, name, price
 * and its purchase details.
 */

class PurchaseItemVM(
    private val context: Context, productRelatedPurchases: BillingSkuRelatedPurchases
) {
    @JvmField
    val skuProductName = ObservableField<String>()

    @JvmField
    val skuProductState = ObservableField<String>()
    private val skuProductDetails: BillingSkuDetails
    private val productPurchaseDetails: List<BillingPurchaseDetails>

    /**
     * Initializes Product Item and sets its respective values and tells the updates to the view.
     */
    private fun init() {
        if (skuProductDetails.skuID == BillingConstants.SKU_BUY_APPLE) {
            // This is Apple.
            val productName = context.resources
                .getQuantityString(R.plurals.apples, productPurchaseDetails.size)
            skuProductName.set(productName)
            val productState = context.resources
                .getQuantityString(
                    R.plurals.qty,
                    productPurchaseDetails.size,
                    productPurchaseDetails.size
                )
            skuProductState.set(productState)
        } else {
            // This is Popcorn.
            skuProductName.set(context.getString(R.string.unlimited_popcorn))
            checkPopcornPurchaseStatus()
        }
    }

    /**
     * Checks whether the Popcorn Product Item was purchased or not and it tells its Purchase Status
     * the updates to the view.
     */
    @SuppressLint("StringFormatInvalid")
    private fun checkPopcornPurchaseStatus() {
        // Unlimited popcorn was not purchased yet.
        if (productPurchaseDetails.size <= 0) {
            skuProductState.set(context.getString(R.string.not_purchased_yet))
            return
        }
        var productPurchaseTimeInMillis = productPurchaseDetails[productPurchaseDetails.size - 1].purchaseTime
        // Test Subscriptions are valid for 5 minutes from the purchase time. For
        // production release, add 30 days to the purchase time that gives the expiry date of
        // subscription
        productPurchaseTimeInMillis = productPurchaseTimeInMillis + DateTimeUtil.FIVE_MINUTES_IN_MILLIS
        // Expiry Date of Subscription
        val productExpiryDateTime = DateTimeUtil.getDateTime(productPurchaseTimeInMillis)
        // Unlimited popcorn purchase was expired if true.
        if (DateTimeUtil.isDateTimePast(productPurchaseTimeInMillis)) {
            skuProductState.set(
                context.getString(R.string.purchase_expired, productExpiryDateTime)
            )
            return
        }
        // Unlimited popcorn purchase is active. For apple, it can be bought multiple times.
        skuProductState.set(
            context.getString(R.string.purchased_with_expiry, productExpiryDateTime)
        )
    }

    companion object {
        @JvmStatic
        @BindingAdapter("purchaseItemSrcCompat")
        fun setPurchaseItemSrcCompat(
            iv: ImageView, skuProductName: String
        ) {
            val drawableResId =
                if (skuProductName == iv.context.getString(R.string.unlimited_popcorn)) R.drawable.baseline_payment_24 else R.drawable.baseline_payment_24
            iv.setImageResource(drawableResId)
        }
    }

    /**
     * Initializes Product Item attributes.
     *
     * @param context An AppCompatActivity Context.
     * @param productRelatedPurchases contains Products with its Sku Details and its related
     * Purchases.
     */
    init {
        skuProductDetails = productRelatedPurchases.billingSkuDetails
        productPurchaseDetails = productRelatedPurchases.billingPurchaseDetails
        init()
    }
}