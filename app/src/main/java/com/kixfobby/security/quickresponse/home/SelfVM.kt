package com.kixfobby.security.quickresponse.home

import android.app.Application
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.androidhiddencamera.HiddenCameraFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.billing.BillingCallback
import com.kixfobby.security.quickresponse.helper.network.NetworkManager.Companion.isOnline
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.store.StoreActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SelfVM @Inject constructor(application: Application, private val appDatabase: AppDatabase) :
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
        //isPremiumPurchased = appDatabase.getIsThisSkuPurchased(BillingConstants.SKU_BUY_APPLE)
    }

    private val myDatabaseReference: DatabaseReference
    private val reference1: DatabaseReference
    private val personId: String? = null
    private var count: Int? = null
    private val mAuth: FirebaseAuth
    private val mAuthListener: AuthStateListener? = null
    private val user1: FirebaseUser?
    private var mHiddenCameraFragment: HiddenCameraFragment? = null


    /*@BindingAdapter("android:drawableEnd")
    public static void setDrawableEnd(@NonNull Button button,
                                      @NonNull Boolean isPremiumPurchased) {
        //setBtnDrawableRightEnd(button, isPremiumPurchased);
    }*/
    /**
     * Sets two features (View Your Purchases and Buy From Store) locked, if Premium Feature
     * Product was not purchased, Unlocked otherwise.
     *
     * @param button             An instance of a Button Widget.
     * @param isPremiumPurchased A boolean value represents whether the Premium Feature Product was
     * purchased or not.
     */
    /*private static void setBtnDrawableRightEnd(Button button, Boolean isPremiumPurchased) {
		if (isPremiumPurchased) {
			button.setCompoundDrawablesWithIntrinsicBounds(
					0,
					0,
					0,
					0);
		} else {
			button.setCompoundDrawablesWithIntrinsicBounds(
					0,
					0,
					R.drawable.ic_lock_outline_white,
					0);
		}
	}*/
    /*@BindingAdapter("android:drawableRight")
    public static void setDrawableRight(@NonNull Button button,
                                        @NonNull Boolean isPremiumPurchased) {
        //setBtnDrawableRightEnd(button, isPremiumPurchased);
    }*/

    /**
     * A view gets this LiveData of Premium Feature purchased or not and observes for
     * changes and updates with it.
     *
     * @return a LiveData of Premium Feature Purchased or not.
     */


    /**
     * Handles Click Events from View.
     *
     * @param v A view in which the click action performed.
     */
    /*fun onClick(v: View) {
        val id = v.id
        when (id) {
            R.id.btn_buy_from_store -> {
                if (checkIsPremiumPurchased(v)) {
                    StoreActivity.start(v.context)
                }
            }
            R.id.btn_view_your_purchases -> {
                if (checkIsPremiumPurchased(v)) {
                    PurchasesActivity.start(v.context)
                }
            }
        }
    }*/
    fun onLongClick(v: View): Boolean {
        when (v.id) {
            R.id.btn_panic -> {
                if (!checkIsPremiumPurchased(v)) {
                    StoreActivity.start(v.context)
                }
                //BaseActivity.showSnackBar(v, "App Premium Purchased!")
                //new Nearby().onDial(v);
                //readCount();
                //addCount(count + 1);
            }
        }
        return false
    }

    /**
     * Launches BillingPremiumDialog if Premium Purchase was not purchased.
     * Shows a SnackBar if there is no Internet Connectivity.
     *
     * @param v A view in which the click action performed.
     * @return whether the Premium Feature Purchased or not.
     */

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkIsPremiumPurchased(v: View): Boolean {
        //val isPurchased = if (isPremiumPurchased.value != null) isPremiumPurchased.value!! else false
        if (/*!isPurchased && */!isOnline(v.context)!!) {
            BaseActivity.showSnackBar(v, R.string.err_no_internet)
            return false
        }
        var uCheck = Pref(v.context).get("checkPaidUser", false)
        if (/*!isPurchased && */uCheck == false) {
            //Toast.makeText(v.context, "!U", Toast.LENGTH_SHORT).show()
            return false
        } else {
            //Toast.makeText(v.context, "U", Toast.LENGTH_SHORT).show()
            return true
        }

        /*if (!isPurchased && !user1!!.email.equals(Self.ad1, ignoreCase = true)) {
            if (!isPurchased && !user1.email.equals(Self.ad2, ignoreCase = true)) {
                if (!isPurchased && !user1.email.equals(Self.ad3, ignoreCase = true)) {
                    if (!isPurchased && !user1.email.equals(Self.ad4, ignoreCase = true)) {
                        if (!isPurchased && !user1.email.equals(Self.ad5, ignoreCase = true)) {
                            if (!isPurchased && !user1.email.equals(Self.ad6, ignoreCase = true)) {
                                if (!isPurchased && !user1.email.equals(Self.ad7, ignoreCase = true)) {
                                    if (!isPurchased && !user1.email.equals(Self.ad8, ignoreCase = true)) {
                                        if (!isPurchased && !user1.email.equals(Self.ad9, ignoreCase = true)) {
                                            if (!isPurchased && !user1.email.equals(Self.ad10, ignoreCase = true)) {

                                                BillingPremiumDialog.show(v.context)
                                                return false
                                            }
                                            return false
                                        }
                                        return false
                                    }
                                    return false
                                }
                                return false
                            }
                            return false
                        }
                        return false
                    }
                    return false
                }
                return false
            }
            return false
        }*/
        //return true
    }
    /*private fun checkIsPremiumPurchased(v: View): Boolean {
        val isPurchased = isPremiumPurchased.value
        if (isPurchased != null) {
            if (!isPurchased && !NetworkManager.isNetworkConnected) {
                BaseActivity.showSnackBar(v, R.string.err_no_internet)
                return false
            }

    }*/

    /*@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        AppDatabase.destroyAppDatabase()
    }*/

    private fun readCount() {
        reference1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: List<Int> = ArrayList()
                for (ds in dataSnapshot.children) {
                    count = ds.getValue(Int::class.java)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun addCount(count: Int) {
        //UsersDb person = new UsersDb(count);
        reference1.setValue(count)
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        //String currentTime = sdf.format(new Date());
        //reference1.child("time").setValue(currentTime);
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
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth.currentUser
        myDatabaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = myDatabaseReference.child(user1!!.uid).child("Click Count")
    }

}