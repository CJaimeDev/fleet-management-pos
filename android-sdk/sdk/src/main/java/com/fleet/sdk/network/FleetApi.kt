package com.fleet.sdk.network

import com.fleet.sdk.models.HeartbeatData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FleetApi {

    @POST("api/heartbeat")
    suspend fun sendHeartbeat(
        @Body heartbeat: HeartbeatData
    ): Response<Map<String, Any>>
}