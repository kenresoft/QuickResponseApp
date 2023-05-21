package com.kixfobby.security.quickresponse.billing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.KixfActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.base.CallbackProvider
import com.kixfobby.security.quickresponse.helper.network.NetworkManager
import com.kixfobby.security.quickresponse.room.database.AppDatabase
import com.kixfobby.security.quickresponse.room.entity.BillingPurchaseDetails
import com.kixfobby.security.quickresponse.room.entity.BillingSkuDetails
import com.kixfobby.security.quickresponse.service.MyUploadService
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.service.worker.WorkExecutor
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides access to BillingClient [.myBillingClient], handles and performs InApp Purchases.
 *
 * @author Vignesh S
 * @version 1.0, 04/03/2018
 * @see [Google Play
 * Billing Library Guide](https://developer.android.com/google/play/billing/billing_library.html)
 *
 * @see [Google Play
 * Billing Training Guide](https://developer.android.com/training/play-billing-library/index.html)
 *
 * @see [Testing
 * InApp and Subscription purchases and Renewal Timing Guide](https://developer.android.com/google/play/billing/billing_testing.html)
 *
 * @see [Google's Play Billing Sample](https://github.com/android/play-billing-samples)
 *
 * @since 1.0
 */
@Singleton
open class BillingManager @RequiresApi(api = Build.VERSION_CODES.N) @Inject constructor(
    // Background work executor
    private val context: Context,
    private val appDatabase: AppDatabase,
    private val networkManager: NetworkManager,
    private val workExecutor: WorkExecutor
) : PurchasesUpdatedListener, CallbackProvider<BillingCallback?>, NetworkManager.NetworkStateCallback {
    // Default value of mBillingClientResponseCode until BillingManager was not yet initialized
    private val myPurchasesResultList: MutableList<Purchase> = ArrayList()

    /**
     * A reference to BillingClient
     */
    private val myBillingClient: BillingClient
    private val billingCallbacks: MutableList<BillingCallback> = ArrayList()
    private val tokensToBeConsumed: MutableSet<String> = HashSet()

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onNetworkAvailable() {
        BaseActivity.printLog(TAG, "onNetworkAvailable: Network Connected")
        connectToPlayBillingService()
    }

    override fun addCallback(cb: BillingCallback?) {
        if (!billingCallbacks.contains(cb)) {
            if (cb != null) {
                billingCallbacks.add(cb)
            }
        }
    }

    override fun removeCallback(cb: BillingCallback?) {
        billingCallbacks.remove(cb)
    }

    /**
     * Clears the resources
     */
    private fun destroy() {
        BaseActivity.printLog(TAG, "Destroying the billing manager.")
        if (myBillingClient.isReady) {
            myBillingClient.endConnection()
        }
        networkManager.removeCallback(this)
    }

    /**
     * Initiates Google Play Billing Service.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun connectToPlayBillingService() {
        BaseActivity.printLog(TAG, "connectToPlayBillingService")
        if (!myBillingClient.isReady) {
            startServiceConnection {

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                BaseActivity.printLog(TAG, "Setup successful. Querying inventory.")
                myPurchasesResultList.clear()
                querySkuDetails()
                queryPurchasesLocally()
                queryPurchasesHistoryAsync()
            }
        }
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through a
     * listener
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun queryPurchasesLocally() {
        val purchasesList: MutableList<Purchase> = ArrayList()
        queryPurchasesAsync(
            purchasesList,
            SkuType.INAPP
        ) {
            // If there are subscriptions supported, we add subscription rows as well
            if (areSubscriptionsSupported()) {
                queryPurchasesAsync(purchasesList, SkuType.SUBS, null)
            }
        }
    }

    /**
     * Queries InApp and Subscribed purchase results from Google Play Locally.
     *
     * @param purchases           this list contains all the product purchases made, has InApp and
     * Subscription purchased results.
     * @param skuType             InApp or Subscription.
     * @param executeWhenFinished Once the InApp product purchase results are given, then
     * subscription based purchase results are queried and results are placed into the [                            ][.myPurchasesResultList]
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun queryPurchasesAsync(
        purchases: MutableList<Purchase>,
        @SkuType skuType: String,
        executeWhenFinished: Runnable?
    ) {
        val purchasesResponseListener =
            PurchasesResponseListener { billingResult: BillingResult, list: List<Purchase>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.addAll(list!!)
                    executeWhenFinished?.run()
                } else {
                    BaseActivity.printLog(
                        TAG, "queryPurchasesAsync() got an error response code: "
                                + billingResult.responseCode
                    )
                    logErrorType(billingResult)
                }
                if (executeWhenFinished == null) {
                    processPurchases(purchases)
                }
            }
        executeServiceRequest { myBillingClient.queryPurchasesAsync(skuType, purchasesResponseListener) }
    }

    /**
     * Has runnable implementation of querying InApp and Subscription purchases from Google Play
     * Remote Server.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun queryPurchasesHistoryAsync() {
        val purchasesList: MutableList<PurchaseHistoryRecord> = ArrayList()
        queryPurchaseHistoryAsync(
            purchasesList,
            SkuType.INAPP
        ) {
            if (areSubscriptionsSupported()) {
                queryPurchaseHistoryAsync(purchasesList, SkuType.SUBS, null)
            }
        }
    }

    /**
     * Queries InApp and Subscribed purchase results from Google Play Remote Server.
     *
     * @param purchases           this list contains all the product purchases made, has InApp and
     * Subscription purchased results.
     * @param skuType             InApp or Subscription.
     * @param executeWhenFinished Once the InApp product purchase results are given, then
     * subscription based purchase results are queried and results are placed into the [                            ][.myPurchasesResultList]
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun queryPurchaseHistoryAsync(
        purchases: MutableList<PurchaseHistoryRecord>,
        @SkuType skuType: String,
        executeWhenFinished: Runnable?
    ) {
        val listener =
            PurchaseHistoryResponseListener { billingResult: BillingResult, list: List<PurchaseHistoryRecord>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    purchases.addAll(list)
                    executeWhenFinished?.run()
                } else {
                    BaseActivity.printLog(
                        TAG, "queryPurchaseHistoryAsync() got an error response code: "
                                + billingResult.responseCode
                    )
                    logErrorType(billingResult)
                }
                if (executeWhenFinished == null) {
                    storePurchaseHistoryRecordsLocally(purchases)
                }

            }
        executeServiceRequest { myBillingClient.queryPurchaseHistoryAsync(skuType, listener) }
    }

    private fun createOrGetFile(
        destination: File,  // e.g., /storage/emulated/0/Android/data/
        fileName: String,  // e.g., tripBook.txt
        folderName: String  // e.g., bookTrip
    ): File {
        val folder = File(destination, folderName)
        // file path = /storage/emulated/0/Android/data/bookTrip/tripBook.txt
        return File(folder, fileName)
    }

    private fun writeFile(context: Context, text: String, file: File) {
        try {
            file.parentFile!!.mkdirs()
            file.bufferedWriter().use { out ->
                out.write(text)
            }
        } catch (e: IOException) {
            Toast.makeText(context, context.getString(R.string.error_message), Toast.LENGTH_LONG).show()
            return
        }
        //Toast.makeText(context, context.getString(R.string.success), Toast.LENGTH_LONG).show()
    }


    /**
     * Stores Purchased Items, consumes consumable items, acknowledges non-consumable items.
     *
     * @param purchases list of Purchase Details returned from the queries.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun processPurchases(purchases: List<Purchase>) {
        if (purchases.isNotEmpty()) {
            BaseActivity.printLog(TAG, "purchase list size: " + purchases.size)
        }
        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                handlePurchase(purchase)

                //// ADD CODE HERE FOR PURCHASE SUCCESSFUL
                //Toast.makeText(context, purchase.originalJson.toString(), Toast.LENGTH_LONG).show()

                val file = createOrGetFile(
                    File(BaseActivity.storageLocation),
                    "Payment Details " + "${KixfActivity().date}" + ".txt",
                    ".payment"
                )
                writeFile(context, purchase.originalJson.toString(), file)

                Pref(context).put("fileFormat", "text")
                var imgUri = Uri.fromFile(file)

                context.startService(
                    Intent(context, MyUploadService::class.java)
                        .putExtra(MyUploadService.EXTRA_FILE_URI, imgUri)
                        .setAction(MyUploadService.ACTION_UPLOAD)
                )

            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                BaseActivity.printLog(
                    TAG,
                    "Received a pending purchase of SKU: " + purchase.skus
                )
                // handle pending purchases, e.g. confirm with users about the pending
                // purchases, prompt them to complete it, etc.
                // TODO: 8/24/2020 handle this in the next release.
            }
        }
        storePurchaseResultsLocally(myPurchasesResultList)
        purchases.forEach(
            Consumer { purchase: Purchase ->
                val sku = purchase.skus.stream().findFirst().orElse("")
                if (sku == BillingConstants.SKU_BUY_APPLE) {
                    handleConsumablePurchasesAsync(purchase)
                } else {
                    acknowledgeNonConsumablePurchasesAsync(purchase)
                }
            })
    }

    /**
     * If you do not acknowledge a purchase, the Google Play Store will provide a refund to the
     * users within a few days of the transaction. Therefore you have to implement
     * [BillingClient.acknowledgePurchaseAsync] inside your app.
     *
     * @param purchase list of Purchase Details returned from the queries.
     */
    private fun acknowledgeNonConsumablePurchasesAsync(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = AcknowledgePurchaseResponseListener { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                BaseActivity.printLog(
                    TAG, "onAcknowledgePurchaseResponse: " + BillingClient.BillingResponseCode.OK
                )
            } else {
                BaseActivity.printLog(
                    TAG, "onAcknowledgePurchaseResponse: "
                            + billingResult.debugMessage
                )
            }
        }
        executeServiceRequest { myBillingClient.acknowledgePurchase(params, listener) }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onPurchasesUpdated(
        billingResult: BillingResult, purchases: List<Purchase>?
    ) {
        BaseActivity.printLog(
            TAG, "onPurchasesUpdate() responseCode: " + billingResult.responseCode
        )
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        } else {
            // Handle any other error codes.
            logErrorType(billingResult)
        }
    }

    /**
     * Adds purchase results to the [.myPurchasesResultList] after successful purchase.
     *
     * @param purchase the purchase result contains Purchase Details.
     */
    private fun handlePurchase(purchase: Purchase) {
        BaseActivity.printLog(TAG, "Got a purchase: $purchase")
        myPurchasesResultList.add(purchase)
    }

    /**
     * Stores Purchase Details on local storage.
     *
     * @param purchases list of Purchase Details returned from the queries.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun storePurchaseResultsLocally(purchases: List<Purchase>) {
        val billingPurchaseDetailsList: MutableList<BillingPurchaseDetails> = ArrayList()
        for (purchase in purchases) {
            val billingPurchaseDetails = BillingPurchaseDetails()
            billingPurchaseDetails.purchaseToken = purchase.purchaseToken
            billingPurchaseDetails.orderID = purchase.orderId
            billingPurchaseDetails.skuID = purchase.skus.stream().findFirst().orElse("")
            billingPurchaseDetails.purchaseTime = purchase.purchaseTime
            billingPurchaseDetailsList.add(billingPurchaseDetails)
        }
        workExecutor.execute { appDatabase.insertPurchaseDetails(billingPurchaseDetailsList) }
    }

    /**
     * Stores Purchase Details on local storage.
     *
     * @param purchases list of Purchase Details returned from the queries.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun storePurchaseHistoryRecordsLocally(purchases: List<PurchaseHistoryRecord>) {
        val billingPurchaseDetailsList: MutableList<BillingPurchaseDetails> = ArrayList()
        for (purchase in purchases) {
            val billingPurchaseDetails = BillingPurchaseDetails()
            billingPurchaseDetails.purchaseToken = purchase.purchaseToken
            billingPurchaseDetails.skuID = purchase.skus.stream().findFirst().orElse("")
            billingPurchaseDetails.purchaseTime = purchase.purchaseTime
            billingPurchaseDetailsList.add(billingPurchaseDetails)
        }
        workExecutor.execute { appDatabase.insertPurchaseDetails(billingPurchaseDetailsList) }
    }

    /**
     * Consumes InApp Product Purchase after successful purchase of InApp Product Purchase. InApp
     * Products cannot be bought after a purchase was made. We need to consume it after a successful
     * purchase, so that we can purchase again and it will become available for the next time we
     * make purchase of the same product that was bought before.
     *
     * @param purchase the purchase result contains Purchase Details.
     */
    private fun handleConsumablePurchasesAsync(purchase: Purchase) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (tokensToBeConsumed.contains(purchase.purchaseToken)) {
            BaseActivity.printLog(TAG, "Token was already scheduled to be consumed - skipping...")
            return
        }
        tokensToBeConsumed.add(purchase.purchaseToken)
        // Generating Consume Response listener
        val listener = ConsumeResponseListener { billingResult: BillingResult, purchaseToken: String ->
            // If billing service was disconnected, we try to reconnect 1 time
            // (feel free to introduce your retry policy here).
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                BaseActivity.printLog(
                    TAG, "onConsumeResponse, Purchase Token: $purchaseToken"
                )
            } else {
                BaseActivity.printLog(
                    TAG, "onConsumeResponse: " + billingResult.debugMessage
                )
            }
        }
        // Consume the purchase async
        val consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        // Creating a runnable from the request to use it inside our connection retry policy below
        executeServiceRequest { myBillingClient.consumeAsync(consumeParams, listener) }
    }

    /**
     * Logs Billing Client Success, Failure and error responses.
     *
     * @param billingResult to identify the states of Billing Client Responses.
     * @see [Google
     * Play InApp Purchase Response Types Guide](https://developer.android.com/google/play/billing/billing_reference.html)
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun logErrorType(billingResult: BillingResult) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.DEVELOPER_ERROR, BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BaseActivity.printLog(
                TAG,
                "Billing unavailable. Make sure your Google Play app is setup correctly"
            )
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                notifyBillingError(R.string.err_service_disconnected)
                connectToPlayBillingService()
            }
            BillingClient.BillingResponseCode.OK -> BaseActivity.printLog(TAG, "Setup successful!")
            BillingClient.BillingResponseCode.USER_CANCELED -> BaseActivity.printLog(
                TAG,
                "User has cancelled Purchase!"
            )
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> notifyBillingError(R.string.err_no_internet)
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> BaseActivity.printLog(
                TAG,
                "Product is not available for purchase"
            )
            BillingClient.BillingResponseCode.ERROR -> BaseActivity.printLog(TAG, "fatal error during API action")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> BaseActivity.printLog(
                TAG,
                "Failure to purchase since item is already owned"
            )
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> BaseActivity.printLog(
                TAG,
                "Failure to consume since item is not owned"
            )
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BaseActivity.printLog(
                TAG,
                "Billing feature is not supported on your device"
            )
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> BaseActivity.printLog(
                TAG,
                "Billing service timeout occurred"
            )
            else -> BaseActivity.printLog(TAG, "Billing unavailable. Please check your device")
        }
    }

    /**
     * Notifies billing error message to all the registered clients.
     *
     * @param id A StringResID [StringRes]
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun notifyBillingError(@StringRes id: Int) {
        BaseActivity.showToast(context, id.toString())
        billingCallbacks.forEach(Consumer { cb: BillingCallback -> cb.onBillingError(context.getString(id)) })
    }

    /**
     * Starts BillingClient Service if not connected already, Or does the tasks written inside the
     * runnable implementation.
     *
     * @param runnable A runnable implementation.
     */
    private fun executeServiceRequest(runnable: Runnable) {
        if (myBillingClient.isReady) {
            runnable.run()
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable)
        }
    }

    /**
     * Makes connection with BillingClient.
     *
     * @param executeOnSuccess A runnable implementation.
     */
    private fun startServiceConnection(executeOnSuccess: Runnable?) {
        myBillingClient.startConnection(
            object : BillingClientStateListener {
                @RequiresApi(api = Build.VERSION_CODES.N)
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    // The billing client is ready. You can query purchases here.
                    BaseActivity.printLog(TAG, "Setup finished")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        executeOnSuccess?.run()
                    }
                    logErrorType(billingResult)
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
    }

    /**
     * Queries for in-app and subscriptions SKU details.
     */
    private fun querySkuDetails() {
        val skuResultMap: MutableMap<String, SkuDetails> = HashMap()
        val subscriptionSkuList = BillingConstants.getSkuList(SkuType.SUBS)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(subscriptionSkuList).setType(SkuType.SUBS)
        querySkuDetailsAsync(
            skuResultMap,
            params,
            SkuType.SUBS
        ) {
            val inAppSkuList = BillingConstants.getSkuList(SkuType.INAPP)
            val params1 = SkuDetailsParams.newBuilder()
            params1.setSkusList(inAppSkuList).setType(SkuType.INAPP)
            querySkuDetailsAsync(skuResultMap, params1, SkuType.INAPP, null)
        }
    }

    /**
     * Queries SKU Details from Google Play Remote Server of SKU Types (InApp and Subscription).
     *
     * @param skuResultLMap       contains SKU ID and Price Details returned by the sku details query.
     * @param params              contains list of SKU IDs and SKU Type (InApp or Subscription).
     * @param billingType         InApp or Subscription.
     * @param executeWhenFinished contains query for InApp SKU Details that will be run after
     */
    private fun querySkuDetailsAsync(
        skuResultLMap: MutableMap<String, SkuDetails>,
        params: SkuDetailsParams.Builder,
        @SkuType billingType: String,
        executeWhenFinished: Runnable?
    ) {
        val listener =
            SkuDetailsResponseListener { billingResult: BillingResult, skuDetailsList: List<SkuDetails>? ->
                // Process the result.
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                    BaseActivity.printLog(
                        TAG,
                        "Unsuccessful query for type: "
                                + billingType
                                + ". Error code: "
                                + billingResult.responseCode
                    )
                } else if (skuDetailsList != null && skuDetailsList.size > 0) {
                    for (skuDetails in skuDetailsList) {
                        skuResultLMap[skuDetails.sku] = skuDetails
                    }
                }
                if (executeWhenFinished != null) {
                    executeWhenFinished.run()
                    return@SkuDetailsResponseListener
                }
                if (skuResultLMap.isEmpty()) {
                    BaseActivity.printLog(
                        TAG, "sku error: " + context.getString(R.string.err_no_sku)
                    )
                } else {
                    BaseActivity.printLog(TAG, "storing sku list locally")
                    storeSkuDetailsLocally(skuResultLMap)
                }
            }
        // Creating a runnable from the request to use it inside our connection retry policy below
        executeServiceRequest { myBillingClient.querySkuDetailsAsync(params.build(), listener) }
    }

    /**
     * Start a purchase flow.
     *
     * @param activity   requires activity class to initiate purchase flow.
     * @param skuDetails The SKU Details registered in the Google Play Developer Console.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun initiatePurchaseFlow(activity: Activity, skuDetails: SkuDetails) {
        if (skuDetails.type == SkuType.SUBS && areSubscriptionsSupported()
            || skuDetails.type == SkuType.INAPP
        ) {
            val purchaseParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build()
            executeServiceRequest {
                BaseActivity.printLog(TAG, "Launching in-app purchase flow.")
                myBillingClient.launchBillingFlow(activity, purchaseParams)
            }
        }
    }

    /**
     * Checks if subscriptions are supported for current client.
     *
     *
     * Note: This method does not automatically retry for RESULT_SERVICE_DISCONNECTED. It is only
     * used in unit tests and after queryPurchases execution, which already has a retry-mechanism
     * implemented.
     *
     * @return boolean value of whether the subscription is supported or not.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun areSubscriptionsSupported(): Boolean {
        val billingResult = myBillingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            BaseActivity.printLog(
                TAG, "areSubscriptionsSupported() got an error response: "
                        + billingResult.responseCode
            )
            notifyBillingError(R.string.err_subscription_not_supported)
        }
        return billingResult.responseCode == BillingClient.BillingResponseCode.OK
    }

    /**
     * Stores SKU Details on local storage.
     *
     * @param skuDetailsMap Map of SKU Details returned from the queries.
     */
    private fun storeSkuDetailsLocally(skuDetailsMap: Map<String, SkuDetails>) {
        val billingSkuDetailsList: MutableList<BillingSkuDetails> = ArrayList()
        for (key in skuDetailsMap.keys) {
            val skuDetail = skuDetailsMap[key]
            if (skuDetail != null) {
                val billingSkuDetails = BillingSkuDetails()
                billingSkuDetails.skuID = skuDetail.sku
                billingSkuDetails.skuType = if (skuDetail.type == SkuType.SUBS) SkuType.SUBS else SkuType.INAPP
                billingSkuDetails.skuPrice = skuDetail.price
                billingSkuDetails.originalJson = skuDetail.originalJson
                billingSkuDetailsList.add(billingSkuDetails)
            }
        }
        workExecutor.execute { appDatabase.insertSkuDetails(billingSkuDetailsList) }
    }

    /*override fun onNetworkLost() {
        Toast.makeText(context, "NETWORK LOST2!", Toast.LENGTH_SHORT).show()
    }
*/
    companion object {
        @JvmField
        val TAG = BillingManager::class.java.name
    }

    /**
     * Initializes BillingClient, makes connection and queries sku details, purchase details from
     * Google Play Remote Server, gets purchase details from Google Play Cache.
     *
     * @param context      activity or application context.
     * @param workExecutor An executor with fixed thread pool handles background works.
     */
    init {
        BaseActivity.printLog(TAG, "Creating Billing client.")
        myBillingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        // clears billing manager when the jvm exits or gets terminated.
        Runtime.getRuntime().addShutdownHook(Thread { destroy() })
        // starts play billing service connection
        connectToPlayBillingService()
        // Watches network changes and initiates billing service connection
        // if not started before...
        networkManager.addCallback(this)
    }
}