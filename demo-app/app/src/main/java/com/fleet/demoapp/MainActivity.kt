package com.fleet.demoapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fleet.sdk.FleetSDK

class MainActivity : AppCompatActivity() {

    private var transactionCount = 0

    private lateinit var tvDeviceInfo: TextView
    private lateinit var tvTransactionCount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnSimulateTransaction: Button
    private lateinit var btnSendHeartbeat: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar vistas
        tvDeviceInfo = findViewById(R.id.tvDeviceInfo)
        tvTransactionCount = findViewById(R.id.tvTransactionCount)
        tvStatus = findViewById(R.id.tvStatus)
        btnSimulateTransaction = findViewById(R.id.btnSimulateTransaction)
        btnSendHeartbeat = findViewById(R.id.btnSendHeartbeat)

        // Mostrar info del dispositivo
        updateDeviceInfo()

        // Botón simular transacción
        btnSimulateTransaction.setOnClickListener {
            transactionCount++
            tvTransactionCount.text = transactionCount.toString()
            Toast.makeText(this, "Transacción simulada", Toast.LENGTH_SHORT).show()
        }

        // Botón enviar heartbeat
        btnSendHeartbeat.setOnClickListener {
            sendHeartbeat()
        }
    }

    private fun updateDeviceInfo() {
        val info = """
            Modelo: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            Android: ${android.os.Build.VERSION.RELEASE}
            SDK iniciado automáticamente
            Heartbeats cada 5 minutos
        """.trimIndent()

        tvDeviceInfo.text = info
    }

    private fun sendHeartbeat() {
        tvStatus.text = "Estado: Enviando heartbeat..."

        FleetSDK.sendHeartbeat(
            transactionsCount = transactionCount,
            failedLoginAttempts = 0,
            location = "Demo Location - Chile",
            onSuccess = {
                runOnUiThread {
                    tvStatus.text = "Estado: ✅ Heartbeat enviado correctamente"
                    Toast.makeText(this, "Heartbeat enviado!", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    tvStatus.text = "Estado: ❌ Error: ${error.message}"
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}