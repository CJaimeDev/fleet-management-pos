package com.fleet.services

import com.fleet.database.Alerts
import com.fleet.database.Terminals
import com.fleet.models.Alert
import com.fleet.models.HeartbeatRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object AlertService {
    
    enum class AlertType {
        BATTERY_LOW,
        BATTERY_CRITICAL,
        TERMINAL_OFFLINE,
        STORAGE_LOW,
        NETWORK_ISSUES,
        UNAUTHORIZED_ACCESS
    }
    
    enum class Severity {
        INFO, WARNING, CRITICAL
    }
    
    fun checkAndGenerateAlerts(deviceId: String, heartbeat: HeartbeatRequest) {
        transaction {
            val location = Terminals.selectAll().where { Terminals.deviceId eq deviceId }
                .singleOrNull()
                ?.get(Terminals.location)
            
            // Batería crítica
            if (heartbeat.batteryLevel < 10 && !heartbeat.batteryCharging) {
                createAlertIfNotExists(
                    deviceId = deviceId,
                    alertType = AlertType.BATTERY_CRITICAL.name,
                    severity = Severity.CRITICAL.name,
                    message = "Batería crítica (${heartbeat.batteryLevel}%) - Posible apagado inminente",
                    location = location
                )
            }
            // Batería baja
            else if (heartbeat.batteryLevel < 20 && !heartbeat.batteryCharging) {
                createAlertIfNotExists(
                    deviceId = deviceId,
                    alertType = AlertType.BATTERY_LOW.name,
                    severity = Severity.WARNING.name,
                    message = "Batería baja (${heartbeat.batteryLevel}%)",
                    location = location
                )
            }
            
            // Storage bajo
            val storageGB = heartbeat.storageAvailable / (1024 * 1024 * 1024)
            if (storageGB < 1) {
                createAlertIfNotExists(
                    deviceId = deviceId,
                    alertType = AlertType.STORAGE_LOW.name,
                    severity = Severity.WARNING.name,
                    message = "Espacio en disco bajo (${storageGB}GB libres)",
                    location = location
                )
            }
            
            // Red débil
            if (heartbeat.signalStrength != null && heartbeat.signalStrength < -90) {
                createAlertIfNotExists(
                    deviceId = deviceId,
                    alertType = AlertType.NETWORK_ISSUES.name,
                    severity = Severity.WARNING.name,
                    message = "Señal débil detectada (${heartbeat.signalStrength} dBm)",
                    location = location
                )
            }

            if (heartbeat.failedLoginAttempts > 3) {
                createAlertIfNotExists(
                    deviceId = deviceId,
                    alertType = AlertType.UNAUTHORIZED_ACCESS.name,
                    severity = Severity.CRITICAL.name,
                    message = "Detectados ${heartbeat.failedLoginAttempts} intentos fallidos de acceso",
                    location = location
                )
            }
        }
    }
    
    private fun createAlertIfNotExists(
        deviceId: String,
        alertType: String,
        severity: String,
        message: String,
        location: String?
    ) {
        val existingAlert = Alerts.selectAll().where {
            (Alerts.deviceId eq deviceId) and
            (Alerts.alertType eq alertType) and
            (Alerts.resolved eq false)
        }.singleOrNull()
        
        if (existingAlert == null) {
            Alerts.insert {
                it[Alerts.deviceId] = deviceId
                it[Alerts.alertType] = alertType
                it[Alerts.severity] = severity
                it[Alerts.message] = message
                it[Alerts.location] = location
                it[resolved] = false
                it[createdAt] = Instant.now()
            }
        }
    }
    
    fun getAllAlerts(
        severity: String? = null,
        resolved: Boolean? = null,
        deviceId: String? = null
    ): List<Alert> {
        return transaction {
            var query = Alerts.selectAll()
            
            if (severity != null) {
                query = query.andWhere { Alerts.severity eq severity }
            }
            if (resolved != null) {
                query = query.andWhere { Alerts.resolved eq resolved }
            }
            if (deviceId != null) {
                query = query.andWhere { Alerts.deviceId eq deviceId }
            }
            
            query.orderBy(Alerts.createdAt to SortOrder.DESC)
                .map { mapRowToAlert(it) }
        }
    }
    
    fun getActiveAlerts(): List<Alert> {
        return getAllAlerts(resolved = false)
    }
    
    fun resolveAlert(alertId: Int): Alert? {
        return transaction {
            val updated = Alerts.update({ Alerts.id eq alertId }) {
                it[resolved] = true
                it[resolvedAt] = Instant.now()
            }
            
            if (updated > 0) {
                Alerts.selectAll().where { Alerts.id eq alertId }
                    .singleOrNull()
                    ?.let { mapRowToAlert(it) }
            } else null
        }
    }
    
    private fun mapRowToAlert(row: ResultRow): Alert {
        return Alert(
            id = row[Alerts.id].value,
            deviceId = row[Alerts.deviceId],
            alertType = row[Alerts.alertType],
            severity = row[Alerts.severity],
            message = row[Alerts.message],
            location = row[Alerts.location],
            resolved = row[Alerts.resolved],
            createdAt = row[Alerts.createdAt].toString(),
            resolvedAt = row[Alerts.resolvedAt]?.toString()
        )
    }
};