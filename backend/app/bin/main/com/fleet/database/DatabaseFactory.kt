package com.fleet.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    
    fun init(
        host: String = "localhost",
        port: Int = 5432,
        dbName: String = "fleet_management",
        user: String = "fleet_user",
        password: String = "fleet_pass"
    ) {
        val database = Database.connect(createHikariDataSource(host, port, dbName, user, password))
        
        transaction(database) {
            SchemaUtils.create(Terminals, Heartbeats, Alerts, TransactionsByHour, AppVersions)
        }
    }
    
    private fun createHikariDataSource(
        host: String,
        port: Int,
        dbName: String,
        user: String,
        password: String
    ): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"
            driverClassName = "org.postgresql.Driver"
            username = user
            this.password = password
            maximumPoolSize = 10
            minimumIdle = 2
            isAutoCommit = true
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            
            validate()
        }
        
        return HikariDataSource(config)
    }
}