package com.fleet.demoapp

import android.app.Application
import com.fleet.sdk.FleetSDK

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar el SDK
        FleetSDK.initialize(
            context = this,
            serverUrl = "http://TU_SERVIDOR:8080/",  // Cambia esta URL
            heartbeatIntervalMinutes = 5L
        )
    }
}
