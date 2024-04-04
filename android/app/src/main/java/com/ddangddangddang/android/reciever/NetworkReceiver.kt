package com.ddangddangddang.android.reciever

import android.net.ConnectivityManager
import android.net.Network

class NetworkReceiver(private val onConnected: () -> Unit, private val onLost: () -> Unit) :
    ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
        onConnected()
    }

    override fun onLost(network: Network) {
        onLost()
    }
}
