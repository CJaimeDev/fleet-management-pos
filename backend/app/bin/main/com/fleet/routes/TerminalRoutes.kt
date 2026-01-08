package com.fleet.routes

import com.fleet.models.*
import com.fleet.services.TerminalService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.terminalRoutes() {
    
    route("/api") {
        
        // POST /api/heartbeat
        post("/heartbeat") {
            try {
                val request = call.receive<HeartbeatRequest>()
                val terminal = TerminalService.processHeartbeat(request)
                
                call.respond(
                    HttpStatusCode.OK,
                    TerminalResponse(
                        success = true,
                        data = terminal,
                        message = "Heartbeat received"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        success = false,
                        message = "Error processing heartbeat: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/terminals
        get("/terminals") {
            try {
                val status = call.request.queryParameters["status"]
                val terminals = TerminalService.getAllTerminals(status)
                
                call.respond(
                    HttpStatusCode.OK,
                    TerminalListResponse(
                        success = true,
                        data = terminals
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching terminals: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/terminals/:id
        get("/terminals/{id}") {
            try {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Missing terminal ID")
                )
                
                val terminal = TerminalService.getTerminalById(id)
                
                if (terminal != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        TerminalResponse(success = true, data = terminal)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(success = false, message = "Terminal not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching terminal: ${e.message}"
                    )
                )
            }
        }
        
        // PUT /api/terminals/:id
        put("/terminals/{id}") {
            try {
                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Missing terminal ID")
                )
                
                val request = call.receive<UpdateTerminalRequest>()
                
                if (request.location != null) {
                    val updated = TerminalService.updateTerminalLocation(id, request.location)
                    
                    if (updated != null) {
                        call.respond(
                            HttpStatusCode.OK,
                            TerminalResponse(success = true, data = updated)
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(success = false, message = "Terminal not found")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(success = false, message = "No fields to update")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error updating terminal: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/stats
        get("/stats") {
            try {
                val stats = TerminalService.getFleetStats()
                
                call.respond(
                    HttpStatusCode.OK,
                    StatsResponse(success = true, data = stats)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching stats: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/health
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "ok",
                    timestamp = java.time.Instant.now().toString(),
                    database = "connected"
                )
            )
        }
        
        // GET /api/charts/uptime
        get("/charts/uptime") {
            try {
                val data = TerminalService.getUptimeData()
                
                call.respond(
                    HttpStatusCode.OK,
                    UptimeChartResponse(success = true, data = data)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching uptime data: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/charts/transactions
        get("/charts/transactions") {
            try {
                val data = TerminalService.getTransactionsByHour()
                
                call.respond(
                    HttpStatusCode.OK,
                    TransactionsChartResponse(success = true, data = data)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching transactions data: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/charts/versions
        get("/charts/versions") {
            try {
                val data = TerminalService.getVersionDistribution()
                
                call.respond(
                    HttpStatusCode.OK,
                    VersionsResponse(success = true, data = data)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching versions data: ${e.message}"
                    )
                )
            }
        }
    }
}