package com.fleet.routes

import com.fleet.websocket.WebSocketManager
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*

fun Route.webSocketRoutes() {
    
    webSocket("/ws/dashboard") {
        val sessionId = UUID.randomUUID().toString()
        
        try {
            WebSocketManager.registerConnection(sessionId, this)
            
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        println("Received message from $sessionId: $text")
                        
                        if (text == "ping") {
                            send(Frame.Text("pong"))
                        }
                    }
                    is Frame.Close -> {
                        println("WebSocket closed for session: $sessionId")
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            println("WebSocket error for session $sessionId: ${e.message}")
        } finally {
            WebSocketManager.unregisterConnection(sessionId)
        }
    }
}