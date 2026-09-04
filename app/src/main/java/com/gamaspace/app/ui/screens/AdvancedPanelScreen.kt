package com.gamaspace.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Panel avanzado con todas las opciones disponibles
 */
@Composable
fun AdvancedPanelScreen() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0E27))
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "ADVANCED",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF6B00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(getAdvancedOptions()) { option ->
                AdvancedOptionCard(option = option)
            }
        }
    }
}

@Composable
fun AdvancedOptionCard(option: AdvancedOption) {
    var isEnabled by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1F3A))
                .clickable { if (option.available) isEnabled = !isEnabled }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (option.available) Color.White else Color(0xFF666666)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.status,
                        fontSize = 11.sp,
                        color = when (option.status) {
                            "AVAILABLE" -> Color(0xFF4CAF50)
                            "ROOT REQUIRED" -> Color(0xFFFF6B00)
                            "SHIZUKU REQUIRED" -> Color(0xFFFFEB3B)
                            else -> Color(0xFF888888)
                        }
                    )
                }

                if (option.available && isEnabled) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Enabled",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (!option.available) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFFFF6B00),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

data class AdvancedOption(
    val name: String,
    val status: String,
    val available: Boolean
)

fun getAdvancedOptions(): List<AdvancedOption> {
    return listOf(
        // SYSTEM
        AdvancedOption("ANIMATIONS", "AVAILABLE", true),
        AdvancedOption("DISPLAY REFRESH", "AVAILABLE", true),
        AdvancedOption("DND MODE", "AVAILABLE", true),
        AdvancedOption("BRIGHTNESS", "AVAILABLE", true),
        
        // APPS
        AdvancedOption("CLOSE BACKGROUND APPS", "AVAILABLE", true),
        AdvancedOption("CLEAR CACHE", "AVAILABLE", true),
        
        // WIFI
        AdvancedOption("WI-FI DIAGNOSTICS", "AVAILABLE", true),
        AdvancedOption("CHANNEL ANALYZER", "AVAILABLE", true),
        AdvancedOption("LOW LATENCY MODE", "AVAILABLE", true),
        
        // ADVANCED - REQUIRES ROOT/SHIZUKU
        AdvancedOption("CPU GOVERNOR", "ROOT REQUIRED", false),
        AdvancedOption("GPU FREQUENCY", "ROOT REQUIRED", false),
        AdvancedOption("THERMAL CONTROL", "ROOT REQUIRED", false),
        AdvancedOption("ZRAM MANAGEMENT", "ROOT REQUIRED", false),
        AdvancedOption("I/O SCHEDULER", "ROOT REQUIRED", false)
    )
}
