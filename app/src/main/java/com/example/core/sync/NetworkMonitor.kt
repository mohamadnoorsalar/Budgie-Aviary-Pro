package com.example.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

data class NetworkState(
    val isOnline: Boolean = false,
    val connectionType: ConnectionType = ConnectionType.NONE,
    val lastOnlineTimestamp: Long = System.currentTimeMillis()
)

class NetworkMonitor private constructor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkState = MutableStateFlow(getCurrentNetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val onNetworkRestoredListeners = mutableListOf<() -> Unit>()

    init {
        registerNetworkCallback()
    }

    private fun getCurrentNetworkState(): NetworkState {
        val cm = connectivityManager ?: return NetworkState(isOnline = false)
        val activeNetwork = cm.activeNetwork ?: return NetworkState(isOnline = false)
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkState(isOnline = false)

        val isOnline = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

        val type = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> if (isOnline) ConnectionType.WIFI else ConnectionType.NONE
        }

        return NetworkState(
            isOnline = isOnline,
            connectionType = type,
            lastOnlineTimestamp = if (isOnline) System.currentTimeMillis() else _networkState.value.lastOnlineTimestamp
        )
    }

    private fun registerNetworkCallback() {
        if (connectivityManager == null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = !_networkState.value.isOnline
                updateState()
                if (wasOffline) {
                    notifyNetworkRestored()
                }
            }

            override fun onLost(network: Network) {
                updateState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateState()
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            // Fallback for restricted environments
        }
    }

    private fun updateState() {
        val updated = getCurrentNetworkState()
        _networkState.value = updated
    }

    fun addOnNetworkRestoredListener(listener: () -> Unit) {
        synchronized(onNetworkRestoredListeners) {
            onNetworkRestoredListeners.add(listener)
        }
    }

    fun removeOnNetworkRestoredListener(listener: () -> Unit) {
        synchronized(onNetworkRestoredListeners) {
            onNetworkRestoredListeners.remove(listener)
        }
    }

    private fun notifyNetworkRestored() {
        scope.launch {
            val listenersCopy = synchronized(onNetworkRestoredListeners) {
                onNetworkRestoredListeners.toList()
            }
            listenersCopy.forEach { listener ->
                try {
                    listener.invoke()
                } catch (_: Exception) {
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        fun getInstance(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
