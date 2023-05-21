package com.kixfobby.security.quickresponse.store

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingManager
import com.kixfobby.security.quickresponse.databinding.AdapterStoreItemBinding
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases

/**
 * StoreAdapter, a RecyclerViewAdapter which provides product item and each product item has its own
 * name and price. Each product has respective buy button if it wasn't already purchased. This is
 * not applicable for inApp Products as it can be purchased multiple times. For subscription based
 * products, "Purchased" will be shown to indicate that it was already purchased, otherwise the buy
 * button will be there to make purchase.
 */
class StoreAdapter
/**
 * Initializes attributes.
 *
 * @param context An Activity Context.
 * @param skuProductsAndPurchasesList Has Sku Products and its related purchases.
 * @param billingManager Provides access to BillingClient which perform Product Purchases from
 * Google Play Billing Library.
 */(
    private val context: Context,
    private val skuProductsAndPurchasesList: MutableList<BillingSkuRelatedPurchases?>,
    private val billingManager: BillingManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val adapterStoreItemBinding: AdapterStoreItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.adapter_store_item,
            parent,
            false
        )
        return ViewHolderStoreProduct(adapterStoreItemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolderStoreProduct = holder as ViewHolderStoreProduct
        val productRelatedPurchases = skuProductsAndPurchasesList[position]
        viewHolderStoreProduct.adapterStoreItemBinding.presenter = productRelatedPurchases?.let { StoreItemVM(context, billingManager, it) }
        viewHolderStoreProduct.adapterStoreItemBinding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return skuProductsAndPurchasesList.size
    }

    /** ViewHolderStoreProduct, which provides product view item.  */
    internal class ViewHolderStoreProduct
    /**
     * Gives product view item and its bindings.
     *
     * @param adapterStoreItemBinding Has bindings for the product view item.
     */(var adapterStoreItemBinding: AdapterStoreItemBinding) : RecyclerView.ViewHolder(adapterStoreItemBinding.root)
}