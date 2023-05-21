/*
 * Copyright 2021 LiteKite Startup. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kixfobby.security.quickresponse.billing

import com.android.billingclient.api.BillingClient.SkuType

/**
 * This class has static fields and methods useful for Google's Play Billing.
 */

object BillingConstants {
    // SKUs for our managed products
    // One time purchase (valid for lifetime)
    const val SKU_UNLOCK_APP_FEATURES = "quick_response_app_product_id"

    // can be purchased many times by consuming
    const val SKU_BUY_APPLE = "qr_2users_product_id"

    // SKU for our subscription
    private const val SKU_POPCORN_UNLIMITED_MONTHLY = "kenneth01"
    //private val IN_APP_SKU = arrayOf(/*SKU_UNLOCK_APP_FEATURES,*/ SKU_BUY_APPLE)
    private val SUBSCRIPTIONS_SKU = arrayOf(SKU_POPCORN_UNLIMITED_MONTHLY)

    /**
     * Gives a list of SKUs based on the type of billing, InApp or Subscription.
     *
     * @param billingType the billing type, InApp or Subscription.
     * @return the list of all SKUs for the billing type specified.
     */
    fun getSkuList(@SkuType billingType: String): List<String> {
        return /*if (billingType == SkuType.INAPP)*/ listOf(*SUBSCRIPTIONS_SKU) /*else listOf(*SUBSCRIPTIONS_SKU)*/
    }
}