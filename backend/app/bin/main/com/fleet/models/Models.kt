package com.fleet.models

import kotlinx.serialization.Serializable

// ============ REQUEST MODELS ============

@Serializable
data class HeartbeatRequest(
    val deviceId: String,
    val timestamp: Long,
    val batteryLevel: Int,
    val batteryCharging: Boolean,
    val networkType: String,
    val signalStrength: Int? = null,
    val storageAvailable: Long,
    val appVersion: String,
    val androidVersion: String,
    val model: String,
    val transactionsCount: Int = 0,
    val failedLoginAttempts: Int = 0,
    val location: String? = null
)

@Serializable
data class UpdateTerminalRequest(
    val location: String? = null
)

// ============ RESPONSE MODELS ============

@Serializable
data class Terminal(
    val id: String,
    val deviceId: String,
    val location: String?,
    val status: String,
    val batteryLevel: Int?,
    val batteryCharging: Boolean,
    val networkType: String?,
    val signalStrength: Int?,
    val model: String?,
    val androidVersion: String?,
    val appVersion: String?,
    val lastSeen: String?,
    val totalTransactions: Int,
    val uptimePercentage24h: Double?
)

@Serializable
data class Alert(
    val id: Int,
    val deviceId: String,
    val alertType: String,
    val severity: String,
    val message: String,
    val location: String?,
    val resolved: Boolean,
    val createdAt: String,
    val resolvedAt: String? = null
)

@Serializable
data class FleetStats(
    val totalTerminals: Int,
    val online: Int,
    val offline: Int,
    val activeAlerts: Int,
    val avgUptimePercentage: Double,
    val totalTransactionsToday: Int,
    val uptimeLastHour: Double
)

@Serializable
data class HealthResponse(
    val status: String,
    val timestamp: String,
    val database: String
)

// ============ API RESPONSES ============

@Serializable
data class TerminalListResponse(
    val success: Boolean,
    val data: List<Terminal>? = null,
    val message: String? = null
)

@Serializable
data class TerminalResponse(
    val success: Boolean,
    val data: Terminal? = null,
    val message: String? = null
)

@Serializable
data class StatsResponse(
    val success: Boolean,
    val data: FleetStats? = null,
    val message: String? = null
)

@Serializable
data class AlertListResponse(
    val success: Boolean,
    val data: List<Alert>? = null,
    val message: String? = null
)

@Serializable
data class AlertResponse(
    val success: Boolean,
    val data: Alert? = null,
    val message: String? = null
)

@Serializable
data class ErrorResponse(
    val success: Boolean,
    val message: String
)
@Serializable
data class UptimeDataPoint(
    val time: String,
    val uptime: Double
)

@Serializable
data class TransactionDataPoint(
    val hour: String,
    val count: Int
)

@Serializable
data class VersionDistribution(
    val version: String,
    val count: Int,
    val percentage: Double,
    val isDeprecated: Boolean = false
)

@Serializable
data class UptimeChartResponse(
    val success: Boolean,
    val data: List<UptimeDataPoint>
)

@Serializable
data class TransactionsChartResponse(
    val success: Boolean,
    val data: List<TransactionDataPoint>
)

@Serializable
data class VersionsResponse(
    val success: Boolean,
    val data: List<VersionDistribution>
)