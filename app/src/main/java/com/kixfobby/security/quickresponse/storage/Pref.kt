package com.kixfobby.security.quickresponse.storage

import android.content.Context
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import com.android.billingclient.api.BillingClient

/**
 * FILES TO EDIT IN FUTURE TIME IF I WANT TO ADD NEW PRODUCT PURCHASES
 *
 * 1. BillingManager.kt;  line138
 * 2. ProductAndPurchasesVM.kt
 * 3. NearbyVm.kt
 * 4. SelfVM.kt
 * 5. BillingPremiumVM.kt
 * 6. CREATE NEW FILE billing2Dao.java
 * 7. AppDatabase.java
 */

class Pref(@NonNull var context: Context) {

    //var one: String = get("bill", "quick_response_app_product_id").toString()

    fun put(key: String, value: String?) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
    }

    fun put(key: String, value: Int) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(key, value).apply()
    }

    fun put(key: String, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(key, value).apply()
    }

    fun put(key: String, value: Long) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(key, value).apply()
    }

    fun put(key: String, value: Set<String?>) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(key, value).apply()
    }

    fun get(key: String, defValue: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defValue)
    }

    fun get(key: String, defValue: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defValue)
    }

    fun get(key: String, defValue: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defValue)
    }

    fun get(key: String, defValue: Long): Long {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defValue)
    }

    fun get(key: String, defValue: Set<String>?): Set<String>? {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(key, defValue)
    }

    fun remove(key: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(key).apply()
    }

    fun clear() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply()
    }

    /*fun getSkuList(@BillingClient.SkuType billingType: String?): List<String> {
        var IN_APP_SKU = arrayOf(SKU_UNLOCK_APP_FEATURES)
        var IN_APP_SKU2 = arrayOf(SKU_UNLOCK_APP_FEATURES2)
        var IN_APP_SKU3 = arrayOf(SKU_UNLOCK_APP_FEATURES3)
        var IN_APP_SKU4 = arrayOf(SKU_UNLOCK_APP_FEATURES4)
        var IN_APP_SKU5 = arrayOf(SKU_UNLOCK_APP_FEATURES5)
        var IN_APP_SKU6 = arrayOf(SKU_UNLOCK_APP_FEATURES6)
        var IN_APP_SKU7 = arrayOf(SKU_UNLOCK_APP_FEATURES7)
        var IN_APP_SKU8 = arrayOf(SKU_UNLOCK_APP_FEATURES8)
        var IN_APP_SKU9 = arrayOf(SKU_UNLOCK_APP_FEATURES9)
        var IN_APP_SKU10 = arrayOf(SKU_UNLOCK_APP_FEATURES10)

        return when (one) {
            SKU_UNLOCK_APP_FEATURES -> listOf(*IN_APP_SKU)
            SKU_UNLOCK_APP_FEATURES2 -> listOf(*IN_APP_SKU2)
            SKU_UNLOCK_APP_FEATURES3 -> listOf(*IN_APP_SKU3)
            SKU_UNLOCK_APP_FEATURES4 -> listOf(*IN_APP_SKU4)
            SKU_UNLOCK_APP_FEATURES5 -> listOf(*IN_APP_SKU5)
            SKU_UNLOCK_APP_FEATURES6 -> listOf(*IN_APP_SKU6)
            SKU_UNLOCK_APP_FEATURES7 -> listOf(*IN_APP_SKU7)
            SKU_UNLOCK_APP_FEATURES8 -> listOf(*IN_APP_SKU8)
            SKU_UNLOCK_APP_FEATURES9 -> listOf(*IN_APP_SKU9)
            SKU_UNLOCK_APP_FEATURES10 -> listOf(*IN_APP_SKU10)
            else -> listOf(*IN_APP_SKU)
        }
    }

    companion object {
        const val SKU_UNLOCK_APP_FEATURES: String = "quick_response_app_product_id"
        const val SKU_UNLOCK_APP_FEATURES2: String = "qr_2users_product_id"
        const val SKU_UNLOCK_APP_FEATURES3: String = "qr_3users_product_id"
        const val SKU_UNLOCK_APP_FEATURES4: String = "qr_4users_product_id"
        const val SKU_UNLOCK_APP_FEATURES5: String = "qr_5users_product_id"
        const val SKU_UNLOCK_APP_FEATURES6: String = "qr_6users_product_id"
        const val SKU_UNLOCK_APP_FEATURES7: String = "qr_7users_product_id"
        const val SKU_UNLOCK_APP_FEATURES8: String = "qr_8users_product_id"
        const val SKU_UNLOCK_APP_FEATURES9: String = "qr_9users_product_id"
        const val SKU_UNLOCK_APP_FEATURES10: String = "qr_10users_product_id"
    }
*/
}