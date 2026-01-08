package com.fleet.sdk.collectors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.fleet.sdk.models.HeartbeatData

class DeviceDataCollector(private val context: Context) {

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun collectHeartbeatData(
        transactionsCount: Int = 0,
        failedLoginAttempts: Int = 0,
        location: String? = null
    ): HeartbeatData {
        return HeartbeatData(
            deviceId = getDeviceId(),
            timestamp = System.currentTimeMillis(),
            batteryLevel = getBatteryLevel(),
            batteryCharging = isBatteryCharging(),
            networkType = getNetworkType(),
            signalStrength = getSignalStrength(),
            storageAvailable = getAvailableStorage(),
            appVersion = getAppVersion(),
            androidVersion = getAndroidVersion(),
            model = getDeviceModel(),
            transactionsCount = transactionsCount,
            failedLoginAttempts = failedLoginAttempts,
            location = location
        )
    }

    private fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isBatteryCharging(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.isCharging
    }

    private fun getNetworkType(): String {
        if (!hasPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            return "UNKNOWN"
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "NONE"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "NONE"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> getCellularType()
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "UNKNOWN"
        }
    }

    private fun getCellularType(): String {
        if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) {
            return "CELLULAR"
        }

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G"
            else -> "CELLULAR"
        }
    }

    private fun getSignalStrength(): Int? {
        // Retorna un valor simulado entre -100 y -50 dBm
        return (-100..-50).random()  // ← BIEN: -100 es menor que -50
    }

    private fun getAvailableStorage(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    private fun getDeviceModel(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL}"
    }
}