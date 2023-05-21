package com.kixfobby.security.quickresponse.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database Entity, has Schema about Purchase Details.
 */

@Entity(tableName = "billing_purchase_details", indices = [Index("sku_id")])
class BillingPurchaseDetails {
    @JvmField
    @PrimaryKey
    @ColumnInfo(name = "purchase_token")
    var purchaseToken = ""

    @JvmField
    @ColumnInfo(name = "order_id")
    var orderID = ""

    @JvmField
    @ColumnInfo(name = "sku_id")
    var skuID = ""

    @JvmField
    @ColumnInfo(name = "purchase_time")
    var purchaseTime: Long = 0
}