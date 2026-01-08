package com.fleet.services

import com.fleet.database.Terminals
import com.fleet.database.Heartbeats
import com.fleet.database.Alerts
import com.fleet.database.TransactionsByHour
import com.fleet.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit
import com.fleet.database.AppVersions


object TerminalService {
    
    fun processHeartbeat(request: HeartbeatRequest): Terminal {
        return transaction {
            val terminalId = "POS-${request.deviceId.take(8)}"
            val now = Instant.now()
            
            val existing = Terminals.selectAll().where { Terminals.deviceId eq request.deviceId }.singleOrNull()
            
            if (existing == null) {
                Terminals.insert {
                    it[id] = terminalId
                    it[deviceId] = request.deviceId
                    it[location] = request.location 
                    it[status] = "online"
                    it[batteryLevel] = request.batteryLevel
                    it[batteryCharging] = request.batteryCharging
                    it[networkType] = request.networkType
                    it[signalStrength] = request.signalStrength
                    it[model] = request.model
                    it[androidVersion] = request.androidVersion
                    it[appVersion] = request.appVersion
                    it[storageAvailable] = request.storageAvailable
                    it[lastSeen] = now
                    it[totalTransactions] = request.transactionsCount
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            } else {
                Terminals.update({ Terminals.deviceId eq request.deviceId }) {
                    it[status] = "online"
                    it[location] = request.location
                    it[batteryLevel] = request.batteryLevel
                    it[batteryCharging] = request.batteryCharging
                    it[networkType] = request.networkType
                    it[signalStrength] = request.signalStrength
                    it[model] = request.model
                    it[androidVersion] = request.androidVersion
                    it[appVersion] = request.appVersion
                    it[storageAvailable] = request.storageAvailable
                    it[lastSeen] = now
                    it[totalTransactions] = request.transactionsCount
                    it[updatedAt] = now
                }
            }
            
            Heartbeats.insert {
                it[deviceId] = request.deviceId
                it[timestamp] = Instant.ofEpochMilli(request.timestamp)
                it[batteryLevel] = request.batteryLevel
                it[batteryCharging] = request.batteryCharging
                it[networkType] = request.networkType
                it[signalStrength] = request.signalStrength
                it[storageAvailable] = request.storageAvailable
                it[appVersion] = request.appVersion
                it[androidVersion] = request.androidVersion
                it[model] = request.model
                it[transactionsCount] = request.transactionsCount
                it[createdAt] = now
            }

            aggregateTransactionsByHour(Instant.ofEpochMilli(request.timestamp), request.transactionsCount)
            
            AlertService.checkAndGenerateAlerts(request.deviceId, request)
            
            getTerminalByDeviceId(request.deviceId)!!
        }
    }

    
    fun getAllTerminals(status: String? = null): List<Terminal> {
        return transaction {
            val query = if (status != null) {
                Terminals.selectAll().where { Terminals.status eq status }
            } else {
                Terminals.selectAll()
            }
            
            query.map { mapRowToTerminal(it) }
        }
    }
    
    fun getTerminalById(id: String): Terminal? {
        return transaction {
            Terminals.selectAll().where { Terminals.id eq id }
                .singleOrNull()
                ?.let { mapRowToTerminal(it) }
        }
    }
    
    fun getTerminalByDeviceId(deviceId: String): Terminal? {
        return transaction {
            Terminals.selectAll().where { Terminals.deviceId eq deviceId }
                .singleOrNull()
                ?.let { mapRowToTerminal(it) }
        }
    }
    
    fun updateTerminalLocation(id: String, location: String): Terminal? {
        return transaction {
            val updated = Terminals.update({ Terminals.id eq id }) {
                it[Terminals.location] = location
                it[updatedAt] = Instant.now()
            }
            
            if (updated > 0) getTerminalById(id) else null
        }
    }
    
    fun getFleetStats(): FleetStats {
    return transaction {
        val total = Terminals.selectAll().count().toInt()
        val online = Terminals.selectAll().where { Terminals.status eq "online" }.count().toInt()
        val offline = total - online
        val activeAlerts = Alerts.selectAll().where { Alerts.resolved eq false }.count().toInt()
        
        val avgUptime = Terminals.selectAll()
            .mapNotNull { it[Terminals.uptimePercentage24h].toDouble() }
            .average()
            .let { if (it.isNaN()) 0.0 else it }
        
        val totalTransactionsToday = Terminals.selectAll()
            .sumOf { it[Terminals.totalTransactions] }
        
        val uptimeLastHour = getUptimeLastHour()  // ← Agrega esta línea
        
        FleetStats(
            totalTerminals = total,
            online = online,
            offline = offline,
            activeAlerts = activeAlerts,
            avgUptimePercentage = avgUptime,
            totalTransactionsToday = totalTransactionsToday,
            uptimeLastHour = uptimeLastHour
        )
    }
}
    
    private fun mapRowToTerminal(row: ResultRow): Terminal {
        return Terminal(
            id = row[Terminals.id],
            deviceId = row[Terminals.deviceId],
            location = row[Terminals.location],
            status = row[Terminals.status],
            batteryLevel = row[Terminals.batteryLevel],
            batteryCharging = row[Terminals.batteryCharging],
            networkType = row[Terminals.networkType],
            signalStrength = row[Terminals.signalStrength],
            model = row[Terminals.model],
            androidVersion = row[Terminals.androidVersion],
            appVersion = row[Terminals.appVersion],
            lastSeen = row[Terminals.lastSeen]?.toString(),
            totalTransactions = row[Terminals.totalTransactions],
            uptimePercentage24h = row[Terminals.uptimePercentage24h].toDouble()
        )
    }

    
fun getUptimeData(): List<UptimeDataPoint> {
    return transaction {
        val now = Instant.now()
        val data = mutableListOf<UptimeDataPoint>()
        
        val allTerminals = Terminals.selectAll().map { 
            Pair(it[Terminals.deviceId], it[Terminals.createdAt])
        }
        
        if (allTerminals.isEmpty()) {
            return@transaction emptyList()
        }
        
        // Generar datos cada 1 hora (últimas 24 horas)
        for (i in 0..23) {
            val periodEnd = now.minus((23 - i).toLong(), ChronoUnit.HOURS)
            val periodStart = periodEnd.minus(1, ChronoUnit.HOURS)
            val timeStr = String.format("%02d:00", periodEnd.atZone(java.time.ZoneId.systemDefault()).hour)
            
            val uptimePercentages = allTerminals.mapNotNull { (deviceId, createdAt) ->
                if (createdAt.isAfter(periodEnd)) {
                    return@mapNotNull null
                }
                
                val effectiveStart = if (createdAt.isAfter(periodStart)) createdAt else periodStart
                val minutesActive = java.time.Duration.between(effectiveStart, periodEnd).toMinutes()
                
                if (minutesActive < 5) {
                    return@mapNotNull null
                }
                
                val heartbeatsReceived = Heartbeats.selectAll().where {
                    (Heartbeats.deviceId eq deviceId) and
                    (Heartbeats.timestamp greaterEq effectiveStart) and
                    (Heartbeats.timestamp less periodEnd)
                }.count()
                
                val expectedHeartbeats = (minutesActive / 5).toInt()
                
                if (expectedHeartbeats > 0) {
                    (heartbeatsReceived.toDouble() / expectedHeartbeats) * 100
                } else {
                    0.0
                }
            }
            
            val avgUptime = if (uptimePercentages.isNotEmpty()) {
                uptimePercentages.average().coerceIn(0.0, 100.0)
            } else {
                0.0
            }
            
            data.add(UptimeDataPoint(timeStr, avgUptime))
        }
        
        data
    }
}

// Obtener transacciones por hora (últimas 9 horas)
fun getTransactionsByHour(): List<TransactionDataPoint> {
    return transaction {
        val now = Instant.now()
        val data = mutableListOf<TransactionDataPoint>()
        
        for (i in 8 downTo 0) {
            val hourStart = now.minus(i.toLong(), ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS)
            val hour = hourStart.atZone(java.time.ZoneId.systemDefault()).hour
            val hourStr = String.format("%02d:00", hour)
            
            // Buscar transacciones reales de esa hora
            val count = TransactionsByHour.selectAll().where {
                TransactionsByHour.hour eq hourStart
            }.singleOrNull()?.get(TransactionsByHour.totalTransactions) ?: 0
            
            data.add(TransactionDataPoint(hourStr, count))
        }
        
        data
    }
}

// Obtener uptime de la última hora (para tiempo real)
fun getUptimeLastHour(): Double {
    return transaction {
        val now = Instant.now()
        val oneHourAgo = now.minus(1, ChronoUnit.HOURS)
        
        val allTerminals = Terminals.selectAll().map { row ->
            Triple(
                row[Terminals.deviceId],
                row[Terminals.createdAt],
                row[Terminals.status]
            )
        }
        
        if (allTerminals.isEmpty()) {
            return@transaction 0.0
        }
        
        val uptimePercentages = allTerminals.mapNotNull { (deviceId, createdAt, _) ->
            // Si la terminal es muy nueva (menos de 5 min), no contarla
            if (createdAt.isAfter(now.minus(5, ChronoUnit.MINUTES))) {
                return@mapNotNull null
            }
            
            val effectiveStart = if (createdAt.isAfter(oneHourAgo)) createdAt else oneHourAgo
            val minutesActive = java.time.Duration.between(effectiveStart, now).toMinutes()
            
            if (minutesActive < 5) {
                return@mapNotNull null
            }
            
            val heartbeatsReceived = Heartbeats.selectAll().where {
                (Heartbeats.deviceId eq deviceId) and
                (Heartbeats.timestamp greaterEq effectiveStart) and
                (Heartbeats.timestamp lessEq now)
            }.count()
            
            val expectedHeartbeats = (minutesActive / 5).toInt()
            
            val uptimePercent = if (expectedHeartbeats > 0) {
                (heartbeatsReceived.toDouble() / expectedHeartbeats) * 100
            } else {
                0.0
            }
            
            println("  Terminal $deviceId: received=$heartbeatsReceived, expected=$expectedHeartbeats, uptime=$uptimePercent%")
            
            uptimePercent
        }
        
        println("DEBUG Uptime Última Hora:")
        println("  Total terminales: ${allTerminals.size}")
        println("  Uptime percentages: $uptimePercentages")
        println("  Average: ${if (uptimePercentages.isNotEmpty()) uptimePercentages.average() else 0.0}")
        
        if (uptimePercentages.isNotEmpty()) {
            uptimePercentages.average().coerceIn(0.0, 100.0)
        } else {
            0.0
        }
    }
}


// Calcular uptime de todas las terminales
fun calculateUptimeForAllTerminals() {
    transaction {
        val now = Instant.now()
        val last24h = now.minus(24, ChronoUnit.HOURS)
        
        Terminals.selectAll().forEach { terminal ->
            val deviceId = terminal[Terminals.deviceId]
            val createdAt = terminal[Terminals.createdAt]
            
            // Calcular tiempo desde creación (no más de 24h)
            val startTime = if (createdAt.isAfter(last24h)) createdAt else last24h
            val minutesSinceStart = java.time.Duration.between(startTime, now).toMinutes()
            
            if (minutesSinceStart < 5) {
                // Terminal muy nueva, no calcular uptime aún
                return@forEach
            }
            
            // Contar heartbeats desde startTime
            val heartbeatsReceived = Heartbeats.selectAll().where {
                (Heartbeats.deviceId eq deviceId) and
                (Heartbeats.timestamp greaterEq startTime)
            }.count()
            
            // Heartbeats esperados según el tiempo transcurrido (uno cada 5 min)
            val expectedHeartbeats = (minutesSinceStart / 5).toInt()
            
            val uptimePercentage = if (expectedHeartbeats > 0) {
                (heartbeatsReceived.toDouble() / expectedHeartbeats) * 100
            } else {
                100.0
            }
            
            // Actualizar el uptime de la terminal (máximo 100%)
            Terminals.update({ Terminals.deviceId eq deviceId }) {
                it[Terminals.uptimePercentage24h] = java.math.BigDecimal(uptimePercentage.coerceIn(0.0, 100.0))
                it[updatedAt] = now
            }
        }
    }
}
// Agregar transacciones del heartbeat a la hora correspondiente
fun aggregateTransactionsByHour(timestamp: Instant, transactionsCount: Int) {
    transaction {
        // Redondear al inicio de la hora
        val hourStart = timestamp.truncatedTo(ChronoUnit.HOURS)
        
        // Buscar si ya existe registro para esta hora
        val existing = TransactionsByHour.selectAll().where {
            TransactionsByHour.hour eq hourStart
        }.singleOrNull()
        
        if (existing != null) {
            // Actualizar contador existente
            TransactionsByHour.update({ TransactionsByHour.hour eq hourStart }) {
                it[totalTransactions] = existing[TransactionsByHour.totalTransactions] + transactionsCount
            }
        } else {
            // Crear nuevo registro
            TransactionsByHour.insert {
                it[hour] = hourStart
                it[totalTransactions] = transactionsCount
                it[createdAt] = Instant.now()
            }
        }
    }
}

fun getVersionDistribution(): List<VersionDistribution> {
    return transaction {
        val total = Terminals.selectAll().count().toDouble()
        
        if (total == 0.0) {
            return@transaction emptyList()
        }
        
        // Agrupar por versión
        Terminals.selectAll()
            .groupBy { it[Terminals.appVersion] ?: "Unknown" }
            .map { (version, terminals) ->
                val count = terminals.size
                val percentage = (count / total) * 100
                
                // Verificar si está marcada como deprecated en la tabla
                val isDeprecated = AppVersions.selectAll().where {
                    AppVersions.version eq version
                }.singleOrNull()?.get(AppVersions.isDeprecated) ?: false
                
                VersionDistribution(
                    version = version,
                    count = count,
                    percentage = percentage,
                    isDeprecated = isDeprecated
                )
            }
            .sortedByDescending { it.count }
    }
}

// Marcar terminales como offline si no han enviado heartbeat en 10 minutos
fun markOfflineTerminals() {
    transaction {
        val now = Instant.now()
        val threshold = now.minus(10, ChronoUnit.MINUTES)
        
        Terminals.update({ Terminals.lastSeen less threshold }) {
            it[status] = "offline"
            it[updatedAt] = now
        }
    }
}
}

