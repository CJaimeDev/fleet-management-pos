package com.fleet

import com.fleet.database.DatabaseFactory
import com.fleet.routes.alertRoutes
import com.fleet.routes.terminalRoutes
import com.fleet.routes.webSocketRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.time.Duration
import com.fleet.services.SchedulerService

fun main() {
    val dbHost = System.getenv("DATABASE_HOST") ?: "localhost"
    val dbPort = System.getenv("DATABASE_PORT")?.toIntOrNull() ?: 5432
    val dbName = System.getenv("DATABASE_NAME") ?: "fleet_management"
    val dbUser = System.getenv("DATABASE_USER") ?: "fleet_user"
    val dbPassword = System.getenv("DATABASE_PASSWORD") ?: "fleet_pass"
    
    DatabaseFactory.init(dbHost, dbPort, dbName, dbUser, dbPassword)
    
    // Iniciar jobs programados
    SchedulerService.startScheduledJobs()
    
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(30)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }
    
    install(StatusPages) {
    exception<Exception> { call, cause ->
        call.respondText(
            text = "500: ${cause.message}",
            status = HttpStatusCode.InternalServerError
        )
        cause.printStackTrace()
    }
}
    
    routing {
        terminalRoutes()
        alertRoutes()
        webSocketRoutes()
        
        get("/") {
            call.respondText(
                """
                Fleet Management API
                ====================
                Status: Running
                Version: 1.0.0
                
                Endpoints:
                - POST   /api/heartbeat
                - GET    /api/terminals
                - GET    /api/terminals/{id}
                - PUT    /api/terminals/{id}
                - GET    /api/stats
                - GET    /api/alerts
                - GET    /api/alerts/active
                - POST   /api/alerts/{id}/resolve
                - GET    /api/health
                - WS     /ws/dashboard
                """.trimIndent(),
                ContentType.Text.Plain
            )
        }
    }
    
    println("Fleet Management Backend started successfully!")
}