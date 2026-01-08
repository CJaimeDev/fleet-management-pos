package com.fleet.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.ReferenceOption
import java.time.Instant

// Tabla: Terminals
object Terminals : Table("terminals") {
    val id = varchar("id", 50)
    val deviceId = varchar("device_id", 100).uniqueIndex()
    val location = varchar("location", 255).nullable()
    val status = varchar("status", 20).default("offline")
    
    val batteryLevel = integer("battery_level").nullable()
    val batteryCharging = bool("battery_charging").default(false)
    
    val networkType = varchar("network_type", 20).nullable()
    val signalStrength = integer("signal_strength").nullable()
    
    val model = varchar("model", 100).nullable()
    val manufacturer = varchar("manufacturer", 100).nullable()
    val androidVersion = varchar("android_version", 20).nullable()
    val appVersion = varchar("app_version", 20).nullable()
    
    val storageTotal = long("storage_total").nullable()
    val storageAvailable = long("storage_available").nullable()
    
    val lastSeen = timestamp("last_seen").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())
    
    val totalTransactions = integer("total_transactions").default(0)
    val uptimePercentage24h = decimal("uptime_percentage_24h", 5, 2).default(java.math.BigDecimal.ZERO)
    
    override val primaryKey = PrimaryKey(id)
}

// Tabla: Heartbeats
object Heartbeats : IntIdTable("heartbeats") {
    val deviceId = varchar("device_id", 50).references(Terminals.deviceId, onDelete = ReferenceOption.CASCADE)
    val timestamp = timestamp("timestamp")
    
    val batteryLevel = integer("battery_level")
    val batteryCharging = bool("battery_charging")
    
    val networkType = varchar("network_type", 20)
    val signalStrength = integer("signal_strength").nullable()
    
    val storageAvailable = long("storage_available")
    
    val appVersion = varchar("app_version", 20)
    val androidVersion = varchar("android_version", 20)
    val model = varchar("model", 100)
    
    val transactionsCount = integer("transactions_count").default(0)
    
    val createdAt = timestamp("created_at").default(Instant.now())
}

// Tabla: Alerts
object Alerts : IntIdTable("alerts") {
    val deviceId = varchar("device_id", 50).references(Terminals.deviceId, onDelete = ReferenceOption.CASCADE)
    val alertType = varchar("alert_type", 50)
    val severity = varchar("severity", 20)
    val message = text("message")
    val location = varchar("location", 255).nullable()
    val resolved = bool("resolved").default(false)
    val createdAt = timestamp("created_at").default(Instant.now())
    val resolvedAt = timestamp("resolved_at").nullable()
}
// Tabla: TransactionsByHour (agregadas)
object TransactionsByHour : Table("transactions_by_hour") {
    val id = integer("id").autoIncrement()
    val hour = timestamp("hour")
    val totalTransactions = integer("total_transactions").default(0)
    val createdAt = timestamp("created_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(id)
    
    init {
        index(true, hour) // Índice único por hora
    }
}

object AppVersions : Table("app_versions") {
    val version = varchar("version", 20)
    val isDeprecated = bool("is_deprecated").default(false)
    val releaseDate = timestamp("release_date").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(version)
}