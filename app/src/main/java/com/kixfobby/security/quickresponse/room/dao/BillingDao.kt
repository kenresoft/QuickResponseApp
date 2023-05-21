package com.kixfobby.security.quickresponse.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kixfobby.security.quickresponse.billing.BillingConstants.SKU_UNLOCK_APP_FEATURES
import com.kixfobby.security.quickresponse.room.entity.BillingPurchaseDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases

/**
 * DAO class, performs database operations and returns result in the form of Objects.
 *
 * @author Vignesh S
 * @version 1.0, 04/03/2018
 * @see [A
 * Single Transaction Guide](https://developer.android.com/reference/android/arch/persistence/room/Transaction.html)
 *
 * @since 1.0
 */
@Dao
interface BillingDao {
    @get:Query("select * from billing_sku_details where sku_id != '$SKU_UNLOCK_APP_FEATURES'")
    @get:Transaction
    val skuRelatedPurchases: LiveData<List<BillingSkuRelatedPurchases?>?>

    @Query("select * from billing_sku_details where sku_id = :skuID")
    fun getSkuDetails(skuID: String): LiveData<BillingSkuDetails?>

    @Query("select exists(select * from billing_purchase_details where sku_id = :skuID)")
    fun getIsThisSkuPurchased(skuID: String): LiveData<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSkuDetails(billingSkuDetails: List<BillingSkuDetails?>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPurchaseDetails(billingPurchaseDetails: List<BillingPurchaseDetails?>)
}