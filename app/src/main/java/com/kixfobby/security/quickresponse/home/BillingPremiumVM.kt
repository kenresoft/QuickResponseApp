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
package com.kixfobby.security.quickresponse.home

import android.app.Application
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.SkuDetails
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingCallback
import com.kixfobby.security.quickresponse.billing.BillingConstants
import com.kixfobby.security.quickresponse.billing.BillingManager
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.util.ContextUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONException
import javax.inject.Inject

/**
 * BillingPremiumVM, a view model which gets Premium Feature Sku Details from local database, It
 * tells to the view about the changes and updates, Handles View Click Event Actions.
 *
 * @author Vignesh S
 * @version 1.0, 10/03/2018
 * @since 1.0
 */
@HiltViewModel
class BillingPremiumVM @Inject constructor(
    application: Application,
    private val appDatabase: AppDatabase,
    private val billingManager: BillingManager
) : AndroidViewModel(application), LifecycleObserver, BillingCallback {
    /**
     * A view gets this Premium Feature LiveData and observes for changes and updates with it.
     *
     * @return a LiveData of Premium Feature Sku Details.
     */
    var premiumSkuDetails: LiveData<BillingSkuDetails?> = MutableLiveData()
        private set

    /**
     * Fetches Premium Feature Sku Details stored in the local database and assigns it to [ ][.premiumSkuDetails] LiveData.
     */
    private fun fetchFromDB() {
        premiumSkuDetails = appDatabase.getSkuDetails(BillingConstants.SKU_UNLOCK_APP_FEATURES)
    }

    /**
     * Handles Click Events from View.
     *
     * @param v A view in which the click action performed.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun onClick(v: View) {
        if (v.id == R.id.btn_billing_buy) {
            // Performs Premium Feature Purchase Flow through BillingClient of Google Play
            // Billing Library.
            if (premiumSkuDetails.value != null) {
                val billingSkuDetails = premiumSkuDetails.value
                try {
                    val skuDetails = SkuDetails(billingSkuDetails!!.originalJson)
                    val activityContext = ContextUtil.getActivity(v.context)
                    if (activityContext != null) {
                        billingManager.initiatePurchaseFlow(activityContext, skuDetails)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Makes a call to get Premium Feature Sku Details from local database.
     *
     * @param application An Application Instance.
     * @param billingManager Provides access to BillingClient which perform Product Purchases from
     * Google Play Billing Library.
     */
    init {
        // Sync with the local database
        fetchFromDB()
    }
}