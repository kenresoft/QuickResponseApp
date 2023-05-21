package com.kixfobby.security.quickresponse.helper.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.kixfobby.security.quickresponse.helper.base.CallbackProvider
import java.util.*
import java.util.function.Consumer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles Network Connectivity, checks whether network is available and notifies network
 * connectivity status to the registered receivers.
 */

@Singleton
class NetworkManager @Inject constructor(context: Context) : CallbackProvider<NetworkManager.NetworkStateCallback?> {
    private val handler = Handler(Looper.getMainLooper())
    private val connMgr: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkStateCallbacks: MutableList<NetworkStateCallback> = ArrayList()
    private val networkCallback: NetworkCallback = object : NetworkCallback() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            notifyNetworkState(network)
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            notifyNetworkState(network)
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun onLost(network: Network) {
            super.onLost(network)
            notifyNetworkState(network)
        }
    }
    private var networkCallbackRegistered = false

    /**
     * Checks the network availability state and notifies the state to all the registered clients.
     *
     * @param network An active network object.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    internal fun notifyNetworkState(network: Network) {
        val isAvailable = connMgr.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (isAvailable == true) {
            handler.post { networkStateCallbacks.forEach(Consumer { obj: NetworkStateCallback -> obj.onNetworkAvailable() }) }
        } else {
            handler.post { networkStateCallbacks.forEach(Consumer { obj: NetworkStateCallback -> obj.onNetworkLost() }) }
        }
    }

    /**
     * Registers a Default Network Callback that will be notified whenever there's a network change
     * happens.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun registerNetworkCallback() {
        if (!networkCallbackRegistered && networkStateCallbacks.size > 0) {
            networkCallbackRegistered = true
            connMgr.registerDefaultNetworkCallback(networkCallback)
        }
    }

    /** Unregisters Default Network Callback.  */
    private fun unregisterNetworkCallback() {
        if (networkCallbackRegistered && networkStateCallbacks.size == 0) {
            networkCallbackRegistered = false
            connMgr.unregisterNetworkCallback(networkCallback)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun addCallback(cb: NetworkStateCallback?) {
        if (!networkStateCallbacks.contains(cb)) {
            if (cb != null) {
                networkStateCallbacks.add(cb)
            }
            registerNetworkCallback()
        }
    }

    override fun removeCallback(cb: NetworkStateCallback?) {
        if (networkStateCallbacks.remove(cb)) {
            unregisterNetworkCallback()
        }
    }

    interface NetworkStateCallback {
        fun onNetworkAvailable() {}
        fun onNetworkLost() {}
    }

    companion object {
        /**
         * Check Network Connectivity through Connectivity Manager.
         *
         * @param context Activity or Application Context.
         * @return boolean value of whether the network has internet connectivity or not.
         */
        @JvmStatic
        @RequiresApi(api = Build.VERSION_CODES.M)
        fun isOnline(context: Context): Boolean? {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (connMgr.activeNetwork != null) {
                connMgr.getNetworkCapabilities(connMgr.activeNetwork)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else false
        }
    }

}