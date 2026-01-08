package com.fleet.websocket

import com.fleet.models.Alert
import com.fleet.models.Terminal
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

object WebSocketManager {
    
    private val connections = ConcurrentHashMap<String, DefaultWebSocketSession>()
    private val json = Json { prettyPrint = false }
    
    @Serializable
    data class WebSocketMessage(
        val type: String,
        val data: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    suspend fun registerConnection(sessionId: String, session: DefaultWebSocketSession) {
        connections[sessionId] = session
        println("WebSocket client connected: $sessionId (Total: ${connections.size})")
        
        sendToSession(
            sessionId,
            WebSocketMessage(
                type = "connected",
                data = """{"message":"Connected to Fleet Management Server","sessionId":"$sessionId"}"""
            )
        )
    }
    
    fun unregisterConnection(sessionId: String) {
        connections.remove(sessionId)
        println("WebSocket client disconnected: $sessionId (Total: ${connections.size})")
    }
    
    private suspend fun sendToSession(sessionId: String, message: WebSocketMessage) {
        try {
            connections[sessionId]?.send(Frame.Text(json.encodeToString(message)))
        } catch (e: Exception) {
            println("Error sending message to session $sessionId: ${e.message}")
            connections.remove(sessionId)
        }
    }
    
    private suspend fun broadcast(message: WebSocketMessage) {
        val disconnected = mutableListOf<String>()
        
        connections.forEach { (sessionId, session) ->
            try {
                session.send(Frame.Text(json.encodeToString(message)))
            } catch (e: ClosedReceiveChannelException) {
                disconnected.add(sessionId)
            } catch (e: Exception) {
                println("Error broadcasting to session $sessionId: ${e.message}")
                disconnected.add(sessionId)
            }
        }
        
        disconnected.forEach { connections.remove(it) }
    }
    
    suspend fun broadcastTerminalUpdate(terminal: Terminal) {
        broadcast(
            WebSocketMessage(
                type = "terminal_update",
                data = json.encodeToString(terminal)
            )
        )
    }
    
    suspend fun broadcastNewAlert(alert: Alert) {
        broadcast(
            WebSocketMessage(
                type = "new_alert",
                data = json.encodeToString(alert)
            )
        )
    }
    
    suspend fun broadcastAlertResolved(alert: Alert) {
        broadcast(
            WebSocketMessage(
                type = "alert_resolved",
                data = json.encodeToString(alert)
            )
        )
    }
}