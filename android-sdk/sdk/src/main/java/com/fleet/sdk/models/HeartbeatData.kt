package com.fleet.sdk.models

import com.google.gson.annotations.SerializedName

data class HeartbeatData(
    @SerializedName("deviceId")
    val deviceId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("batteryLevel")
    val batteryLevel: Int,

    @SerializedName("batteryCharging")
    val batteryCharging: Boolean,

    @SerializedName("networkType")
    val networkType: String,

    @SerializedName("signalStrength")
    val signalStrength: Int?,

    @SerializedName("storageAvailable")
    val storageAvailable: Long,

    @SerializedName("appVersion")
    val appVersion: String,

    @SerializedName("androidVersion")
    val androidVersion: String,

    @SerializedName("model")
    val model: String,

    @SerializedName("transactionsCount")
    val transactionsCount: Int = 0,

    @SerializedName("failedLoginAttempts")
    val failedLoginAttempts: Int = 0,

    @SerializedName("location")
    val location: String? = null
)

