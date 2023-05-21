package com.kixfobby.security.quickresponse.billing

import com.kixfobby.security.quickresponse.BaseActivity

/**
 * Listener to the updates that happens when purchases list was updated or consumption of the item
 * was finished, updates the Billing Client responses and errors to the implemented classes.
 */

interface BillingCallback {
    /**
     * Notifies the error messages of Billing Client.
     *
     * @param error billing client error message.
     */
    fun onBillingError(error: String) {
        BaseActivity.printLog(BillingManager.TAG, error)
    }
}