package com.kixfobby.security.quickresponse.home

import android.app.Application
import android.os.Build
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingCallback
import com.kixfobby.security.quickresponse.billing.BillingConstants
import com.kixfobby.security.quickresponse.helper.network.NetworkManager.Companion.isOnline
import com.kixfobby.security.quickresponse.purchase.PurchasesActivity
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.store.StoreActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * HomeVM, a view model that gives Premium Feature Purchase Status from local database to View,
 * Handles View's Click Event Actions.
 */

@HiltViewModel
class HomeVM @Inject constructor(application: Application, private val appDatabase: AppDatabase) :
    AndroidViewModel(application), LifecycleObserver, BillingCallback {
    /**
     * A view gets this LiveData of Premium Feature purchased or not and observes for changes and
     * updates with it.
     *
     * @return a LiveData of Premium Feature Purchased or not.
     */
    var isPremiumPurchased: LiveData<Boolean> = MutableLiveData()
        private set

    /**
     * Fetches and checks whether the Premium Feature was purchased and stored in the local database
     * and assigns it to [.isPremiumPurchased] LiveData.
     */
    private fun fetchFromDB() {
        isPremiumPurchased = appDatabase.getIsThisSkuPurchased(BillingConstants.SKU_UNLOCK_APP_FEATURES)
    }

    /**
     * Handles Click Events from View.
     *
     * @param v A view in which the click action performed.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_buy_from_store) {
            if (checkIsPremiumPurchased(v)) {
                StoreActivity.start(v.context)
            }
        } else if (id == R.id.btn_view_your_purchases) {
            if (checkIsPremiumPurchased(v)) {
                PurchasesActivity.start(v.context)
            }
        }
    }

    /**
     * Launches BillingPremiumDialog if Premium Purchase was not purchased. Shows a SnackBar if
     * there is no Internet Connectivity.
     *
     * @param v A view in which the click action performed.
     * @return whether the Premium Feature Purchased or not.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkIsPremiumPurchased(v: View): Boolean {
        val isPurchased = if (isPremiumPurchased.value != null) isPremiumPurchased.value!! else false
        if (!isPurchased && !isOnline(v.context)!!) {
            BaseActivity.showSnackBar(v, R.string.err_no_internet)
            return false
        }
        if (!isPurchased) {
            BillingPremiumDialog.show(v.context)
            return false
        }
        return true
    }

    companion object {
        @JvmStatic
        @BindingAdapter("android:drawableEnd")
        fun setDrawableEnd(button: Button, isPremiumPurchased: Boolean) {
            setBtnDrawableRightEnd(button, isPremiumPurchased)
        }

        /**
         * Sets two features (View Your Purchases and Buy From Store) locked, if Premium Feature Product
         * was not purchased, Unlocked otherwise.
         *
         * @param button An instance of a Button Widget.
         * @param isPremiumPurchased A boolean value represents whether the Premium Feature Product was
         * purchased or not.
         */
        private fun setBtnDrawableRightEnd(button: Button, isPremiumPurchased: Boolean) {
            if (isPremiumPurchased) {
                button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                button.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_lock_outline_white, 0
                )
            }
        }

        @BindingAdapter("android:drawableRight")
        fun setDrawableRight(
            button: Button, isPremiumPurchased: Boolean
        ) {
            setBtnDrawableRightEnd(button, isPremiumPurchased)
        }
    }

    /**
     * Makes a call to check whether the Premium Feature was purchased and stored in the local
     * database.
     *
     * @param application An Application Instance.
     */
    init {
        // Sync with the local database
        fetchFromDB()
    }
}