package com.kixfobby.security.quickresponse.room.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.util.*

/**
 * An Entity, has Schema about SKU Details that has related Purchase Details.
 */

class BillingSkuRelatedPurchases {
    @JvmField
    @Embedded
    var billingSkuDetails = BillingSkuDetails()

    @JvmField
    @Relation(parentColumn = "sku_id", entityColumn = "sku_id", entity = BillingPurchaseDetails::class)
    var billingPurchaseDetails: List<BillingPurchaseDetails> = ArrayList()
}