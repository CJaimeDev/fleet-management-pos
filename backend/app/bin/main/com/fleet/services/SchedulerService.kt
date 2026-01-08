package com.fleet.services

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

object SchedulerService {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    fun startScheduledJobs() {
        logger.info("Starting scheduled jobs...")
        
        // Job: Calcular uptime cada 5 minutos
        scope.launch {
            while (isActive) {
                try {
                    delay(5 * 60 * 1000L) // 5 minutos
                    logger.info("Running uptime calculation job...")
                    TerminalService.calculateUptimeForAllTerminals()
                    logger.info("Uptime calculation completed")
                } catch (e: Exception) {
                    logger.error("Error in uptime calculation job: ${e.message}", e)
                }
            }
        }
        
        // Job: Marcar terminales como offline si no han enviado heartbeat en 10 min
        scope.launch {
            while (isActive) {
                try {
                    delay(2 * 60 * 1000L) // 2 minutos
                    logger.info("Running offline detection job...")
                    TerminalService.markOfflineTerminals()
                    logger.info("Offline detection completed")
                } catch (e: Exception) {
                    logger.error("Error in offline detection job: ${e.message}", e)
                }
            }
        }
        
        logger.info("Scheduled jobs started successfully")
    }
    
    fun stopScheduledJobs() {
        logger.info("Stopping scheduled jobs...")
        scope.cancel()
    }
}