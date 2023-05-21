package com.kixfobby.security.quickresponse.purchase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.room.entity.BillingSkuRelatedPurchases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * PurchasesVM, a view model which gets Sku Products List and its related Purchases from local
 * database and updates it to the observing view.
 */

@HiltViewModel
class PurchasesVM @Inject constructor(application: Application, private val appDatabase: AppDatabase) :
    AndroidViewModel(application), LifecycleObserver {
    /**
     * A view gets this [.skuProductsAndPurchasesList] and observes for changes and updates
     * with it.
     *
     * @return a LiveData of Sku Products List and its related Purchases.
     */
    var skuProductsAndPurchasesList: LiveData<List<BillingSkuRelatedPurchases?>?> = MutableLiveData()
        private set

    /**
     * Fetches Sku Products List and its related Purchases stored in the local database and assigns
     * it to [.skuProductsAndPurchasesList] LiveData.
     */
    private fun fetchFromDB() {
        skuProductsAndPurchasesList = appDatabase.skuRelatedPurchases
    }

    /**
     * Makes a call to get Sku Product Details and its related Purchases from local database.
     *
     * @param application application An Application Instance.
     */
    init {
        // Sync with the local database
        fetchFromDB()
    }
}