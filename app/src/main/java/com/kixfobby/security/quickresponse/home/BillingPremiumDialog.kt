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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.DialogBillingPremiumBinding
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import dagger.hilt.android.AndroidEntryPoint

/**
 * BillingPremiumDialog, A PremiumPurchaseDialog which is a BottomSheet which lets user to buy
 * Premium Feature and perform purchase actions from Google Play Billing Library. This Premium
 * Feature is an inApp Product and we won't consume it as it was a one time purchase. Once
 * purchased, no need to purchase again. To buy inApp Products many time, it needs to be consumed
 * otherwise.
 */

@AndroidEntryPoint
class BillingPremiumDialog : BottomSheetDialogFragment() {
    private var dialogBillingPremiumBinding: DialogBillingPremiumBinding? = null

    /**
     * Observes changes and updates about the Premium Feature Sku Product which is stored in the
     * local database.
     *
     *
     * Sets Premium Feature Product Price.
     */
    private val premiumSkuDetailsObserver = Observer { billingSkuDetails: BillingSkuDetails? ->
        if (billingSkuDetails != null) {
            dialogBillingPremiumBinding!!.tvBillingPrice.text = billingSkuDetails.skuPrice
            dialogBillingPremiumBinding!!.executePendingBindings()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialogBillingPremiumBinding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_billing_premium, container, false
        )
        return dialogBillingPremiumBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    /**
     * Initializes Presenter BillingPremiumViewModel, BillingViewModel and registers LifeCycle
     * Observers. Observes Premium Feature Product Sku Details.
     */
    private fun init() {
        val billingPremiumVM = ViewModelProvider(this).get(BillingPremiumVM::class.java)
        dialogBillingPremiumBinding!!.presenter = billingPremiumVM
        this.lifecycle.addObserver(billingPremiumVM)
        billingPremiumVM
            .premiumSkuDetails
            .observe(viewLifecycleOwner, premiumSkuDetailsObserver)
    }

    companion object {
        private val TAG = BillingPremiumDialog::class.java.name

        /**
         * Launches BillingPremiumDialog.
         *
         * @param context An Activity Context.
         */
        fun show(context: Context) {
            if (context is AppCompatActivity) {
                val billingPremiumDialog = BillingPremiumDialog()
                billingPremiumDialog.setStyle(
                    STYLE_NORMAL, R.style.MyBottomSheetDialogTheme
                )
                billingPremiumDialog.show(
                    context.supportFragmentManager, TAG
                )
            }
        }

        /**
         * Dismisses BillingPremiumDialog.
         *
         * @param context An Activity Context.
         */
        fun dismiss(context: Context) {
            if (context is AppCompatActivity) {
                val billingPremiumDialog = context
                    .supportFragmentManager
                    .findFragmentByTag(TAG) as BillingPremiumDialog?
                if (billingPremiumDialog != null && billingPremiumDialog.isAdded) {
                    billingPremiumDialog.dismiss()
                }
            }
        }
    }
}