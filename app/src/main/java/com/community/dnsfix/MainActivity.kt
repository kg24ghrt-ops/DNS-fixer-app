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
                    pingMs = if (mode == ProtectionMode.BPS) 32 else 54,
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
                val vpnIntent = VpnService.prepare(this)
                if (vpnIntent != null) {
                    startActivityForResult(vpnIntent, 1001)
                } else {
                    executeVpnService()
                }
            } else {
                executeBpsMode()
            }
        } else {
            terminateConnections()
        }
    }

    private fun executeBpsMode() {
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
        val intent = Intent(this, BypassVpnService::class.java).apply {
            action = BypassVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        isConnected.value = false
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // Fixed parameter mapping here
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            executeVpnService()
        }
    }
}
