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
package com.kixfobby.security.quickresponse.purchase

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.AdapterPurchaseItemBinding
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases

/**
 * PurchasesAdapter, a RecyclerViewAdapter which provides product item and each product item has its
 * own name and represents its purchases. For inApp Products, quantity of product purchases will be
 * displayed. For subscription based products, the date of expiration will be displayed if it was
 * already purchased. Otherwise "Not Purchased Yet" will be displayed.
 */

class PurchasesAdapter
/**
 * Initializes attributes.
 *
 * @param context An Activity Context.
 * @param skuProductsAndPurchasesList Has Sku Products and its related purchases.
 */(
    private val context: Context,
    private val skuProductsAndPurchasesList: MutableList<BillingSkuRelatedPurchases?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterPurchaseItemBinding: AdapterPurchaseItemBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.adapter_purchase_item, parent, false)
        return ViewHolderPurchaseProduct(adapterPurchaseItemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolderPurchaseProduct = holder as ViewHolderPurchaseProduct
        val productRelatedPurchases = skuProductsAndPurchasesList[position]
        viewHolderPurchaseProduct.adapterPurchaseItemBinding.presenter = productRelatedPurchases?.let { PurchaseItemVM(context, it) }
        viewHolderPurchaseProduct.adapterPurchaseItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return skuProductsAndPurchasesList.size
    }

    /** ViewHolderPurchasedProduct, which provides product view item.  */
    internal class ViewHolderPurchaseProduct
    /**
     * Gives product view item and its bindings.
     *
     * @param adapterPurchaseItemBinding Has bindings for the product view item.
     */(var adapterPurchaseItemBinding: AdapterPurchaseItemBinding) : RecyclerView.ViewHolder(adapterPurchaseItemBinding.root)
}