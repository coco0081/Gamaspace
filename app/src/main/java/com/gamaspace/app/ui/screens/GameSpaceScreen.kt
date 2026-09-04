package com.gamaspace.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Pantalla principal estilo Game Space
 * Interfaz gaming moderna con indicadores en tiempo real
 */
@Composable
fun GameSpaceScreen(
    onGameSelected: (String) -> Unit = {}
) {
    var selectedProfile by remember { mutableStateOf("MAX_PERFORMANCE") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            GameSpaceHeader()

            // Status Panel - Indicadores principales
            GameSpaceStatusPanel()

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Actions - Botones principales
            GameSpaceQuickActions(
                selectedProfile = selectedProfile,
                onProfileChange = { selectedProfile = it },
                onBoostClick = { }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Status
            GameStatusCard(onGameSelected = onGameSelected)
        }
    }
}

@Composable
fun GameSpaceHeader() {
    Text(
        text = "GAME SPACE",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFFFF6B00),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun GameSpaceStatusPanel() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1F3A))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: CPU, RAM, Temp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusIndicator(label = "CPU", value = "--", unit = "%")
                    StatusIndicator(label = "RAM", value = "--", unit = "GB")
                    StatusIndicator(label = "TEMP", value = "34", unit = "°C")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: Ping, Battery, Wi-Fi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusIndicator(label = "PING", value = "24", unit = "ms")
                    StatusIndicator(label = "BATTERY", value = "78", unit = "%")
                    StatusIndicator(label = "SHIZUKU", value = "ON", unit = "")
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun GameSpaceQuickActions(
    selectedProfile: String,
    onProfileChange: (String) -> Unit,
    onBoostClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Profile Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("MAX_PERFORMANCE", "DYNAMIC", "COOL").forEach { profile ->
                GameSpaceButton(
                    text = profile.replace("_", "\n"),
                    isSelected = selectedProfile == profile,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onClick = { onProfileChange(profile) }
                )
            }
        }

        // Main Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameSpaceActionButton(
                icon = Icons.Default.Bolt,
                label = "BOOST",
                modifier = Modifier.weight(1f),
                onClick = onBoostClick
            )
            GameSpaceActionButton(
                icon = Icons.Default.Cloud,
                label = "WI-FI",
                modifier = Modifier.weight(1f)
            )
            GameSpaceActionButton(
                icon = Icons.Default.Memory,
                label = "MONITOR",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameSpaceActionButton(
                icon = Icons.Default.Speed,
                label = "GAMES",
                modifier = Modifier.weight(1f)
            )
            GameSpaceActionButton(
                icon = Icons.Default.Settings,
                label = "ADVANCED",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GameSpaceButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) Color(0xFFFF6B00) else Color(0xFF1A1F3A)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF888888),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun GameSpaceActionButton(
    icon: androidx.compose.material.icons.Icons.Filled,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1F3A))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFFF6B00),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
fun GameStatusCard(
    onGameSelected: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1F3A))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "NO GAME RUNNING",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select a game from library or click GAMES to start",
                    fontSize = 12.sp,
                    color = Color(0xFF666666)
                )
            }
        }
    }
}
