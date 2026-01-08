package com.fleet.routes

import com.fleet.models.*
import com.fleet.services.AlertService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.alertRoutes() {
    
    route("/api/alerts") {
        
        // GET /api/alerts
        get {
            try {
                val severity = call.request.queryParameters["severity"]
                val resolved = call.request.queryParameters["resolved"]?.toBoolean()
                val deviceId = call.request.queryParameters["deviceId"]
                
                val alerts = AlertService.getAllAlerts(severity, resolved, deviceId)
                
                call.respond(
                    HttpStatusCode.OK,
                    AlertListResponse(success = true, data = alerts)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching alerts: ${e.message}"
                    )
                )
            }
        }
        
        // GET /api/alerts/active
        get("/active") {
            try {
                val alerts = AlertService.getActiveAlerts()
                
                call.respond(
                    HttpStatusCode.OK,
                    AlertListResponse(success = true, data = alerts)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error fetching active alerts: ${e.message}"
                    )
                )
            }
        }
        
        // POST /api/alerts/:id/resolve
        post("/{id}/resolve") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Invalid alert ID")
                )
                
                val alert = AlertService.resolveAlert(id)
                
                if (alert != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        AlertResponse(success = true, data = alert)
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(success = false, message = "Alert not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        success = false,
                        message = "Error resolving alert: ${e.message}"
                    )
                )
            }
        }
    }
}