package com.fleet.sdk.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fleet.sdk.collectors.DeviceDataCollector
import com.fleet.sdk.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HeartbeatWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Obtener datos opcionales del inputData
            val transactionsCount = inputData.getInt("transactionsCount", 0)
            val failedLoginAttempts = inputData.getInt("failedLoginAttempts", 0)
            val location = inputData.getString("location")

            // Recolectar datos del dispositivo
            val collector = DeviceDataCollector(applicationContext)
            val heartbeatData = collector.collectHeartbeatData(
                transactionsCount = transactionsCount,
                failedLoginAttempts = failedLoginAttempts,
                location = location
            )

            // Enviar heartbeat al servidor
            val response = ApiClient.getApi().sendHeartbeat(heartbeatData)

            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

