package com.fleet.sdk

import android.content.Context
import androidx.work.*
import com.fleet.sdk.collectors.DeviceDataCollector
import com.fleet.sdk.network.ApiClient
import com.fleet.sdk.worker.HeartbeatWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object FleetSDK {

    private const val HEARTBEAT_WORK_NAME = "fleet_heartbeat_work"
    private var isInitialized = false
    private lateinit var appContext: Context

    /**
     * Inicializa el SDK
     * @param context Contexto de la aplicación
     * @param serverUrl URL del servidor backend (ej: "http://192.168.1.100:8080/")
     * @param heartbeatIntervalMinutes Intervalo entre heartbeats en minutos (default: 5)
     */
    fun initialize(
        context: Context,
        serverUrl: String,
        heartbeatIntervalMinutes: Long = 5L
    ) {
        appContext = context.applicationContext
        ApiClient.initialize(serverUrl)

        // Configurar trabajo periódico
        schedulePeriodicHeartbeat(heartbeatIntervalMinutes)

        isInitialized = true
    }

    /**
     * Envía un heartbeat inmediato
     * @param transactionsCount Número de transacciones procesadas
     * @param failedLoginAttempts Intentos de login fallidos
     * @param location Ubicación del dispositivo (opcional)
     * @param onSuccess Callback de éxito
     * @param onError Callback de error
     */
    fun sendHeartbeat(
        transactionsCount: Int = 0,
        failedLoginAttempts: Int = 0,
        location: String? = null,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        if (!isInitialized) {
            onError?.invoke(Exception("FleetSDK no está inicializado"))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val collector = DeviceDataCollector(appContext)
                val heartbeatData = collector.collectHeartbeatData(
                    transactionsCount = transactionsCount,
                    failedLoginAttempts = failedLoginAttempts,
                    location = location
                )

                val response = ApiClient.getApi().sendHeartbeat(heartbeatData)

                if (response.isSuccessful) {
                    onSuccess?.invoke()
                } else {
                    onError?.invoke(Exception("Error HTTP: ${response.code()}"))
                }
            } catch (e: Exception) {
                onError?.invoke(e)
            }
        }
    }

    /**
     * Detiene el envío periódico de heartbeats
     */
    fun stopHeartbeat() {
        WorkManager.getInstance(appContext)
            .cancelUniqueWork(HEARTBEAT_WORK_NAME)
    }

    private fun schedulePeriodicHeartbeat(intervalMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val heartbeatRequest = PeriodicWorkRequestBuilder<HeartbeatWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(
                HEARTBEAT_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                heartbeatRequest
            )
    }
}

