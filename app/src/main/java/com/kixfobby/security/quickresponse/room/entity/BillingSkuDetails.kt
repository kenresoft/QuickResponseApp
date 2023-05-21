package com.kixfobby.security.quickresponse.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Database Entity, has Schema about SKU Details.
 */

@Entity(tableName = "billing_sku_details", indices = [Index("sku_id")])
class BillingSkuDetails {
    @JvmField
    @PrimaryKey
    @ColumnInfo(name = "sku_id")
    var skuID = ""

    @JvmField
    @ColumnInfo(name = "sku_type")
    var skuType = ""

    @JvmField
    @ColumnInfo(name = "sku_price")
    var skuPrice = ""

    @JvmField
    @ColumnInfo(name = "original_json")
    var originalJson = ""
}