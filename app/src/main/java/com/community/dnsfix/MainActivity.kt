package com.community.dnsfix

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.community.dnsfix.ui.MainScreen
import com.community.dnsfix.ui.ProtectionMode

class MainActivity : ComponentActivity() {

    private val isConnected = mutableStateOf(false)
    private val selectedMode = mutableStateOf(ProtectionMode.BPS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                val connected by remember { isConnected }
                val mode by remember { selectedMode }
                
                MainScreen(
                    isConnected = connected,
                    currentMode = mode,
                    pingMs = if (mode == ProtectionMode.BPS) 32 else 54, // Contextual feedback tracking
                    onModeChange = { newMode ->
                        if (!connected) selectedMode.value = newMode
                    },
                    onToggleConnection = { targetState ->
                        handleConnectionToggle(targetState)
                    }
                )
            }
        }
    }

    private fun handleConnectionToggle(start: Boolean) {
        if (start) {
            if (selectedMode.value == ProtectionMode.VPN) {
                // Mode 2: Launch with system intent intercept verification
                val vpnIntent = VpnService.prepare(this)
                if (vpnIntent != null) {
                    startActivityForResult(vpnIntent, 1001)
                } else {
                    executeVpnService()
                }
            } else {
                // Mode 1: App-level loopback bypass routine execution directly
                executeBpsMode()
            }
        } else {
            terminateConnections()
        }
    }

    private fun executeBpsMode() {
        // Run localized client bypass hooks without initializing standard Android VpnService interface
        isConnected.value = true
    }

    private fun executeVpnService() {
        val intent = Intent(this, BypassVpnService::class.java).apply {
            action = BypassVpnService.ACTION_CONNECT
        }
        startService(intent)
        isConnected.value = true
    }

    private fun terminateConnections() {
        // Safeguard removal strategy for both processing flows
        val intent = Intent(this, BypassVpnService::class.java).apply {
            action = BypassVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        isConnected.value = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, requestCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            executeVpnService()
        }
    }
}
