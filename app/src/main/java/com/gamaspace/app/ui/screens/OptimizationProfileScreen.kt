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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamaspace.app.ui.viewmodel.OptimizationProfileViewModel

/**
 * Pantalla de Perfiles de Optimización
 * Permite gestionar y cambiar entre diferentes perfiles de optimización
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizationProfileScreen(
    viewModel: OptimizationProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfiles de Optimización") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Perfil activo
            if (state.activeProfile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Perfil Activo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.activeProfile!!.profileName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = state.activeProfile!!.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Activo",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.clickable {
                                viewModel.activateProfile(state.activeProfile!!.profileName)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de perfiles
            Text(
                text = "Perfiles Disponibles",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        isActive = profile.isActive,
                        onProfileClick = {
                            viewModel.activateProfile(profile.profileName)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de perfil de optimización
 */
@Composable
fun ProfileCard(
    profile: com.gamaspace.app.data.model.OptimizationProfile,
    isActive: Boolean,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isActive) Color(0xFF1976D2).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.profileName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = profile.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50))
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Activo",
                                tint = Color.White,
                                modifier = Modifier.clickable { onProfileClick() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Opciones del perfil
                ProfileOptions(
                    ramOptimization = profile.ramOptimization,
                    cpuOptimization = profile.cpuOptimization,
                    gpuOptimization = profile.gpuOptimization,
                    batteryOptimization = profile.batteryOptimization,
                    screenBrightness = profile.screenBrightness,
                    backgroundLimit = profile.backgroundLimit
                )
            }
        }
    }
}

/**
 * Opciones de configuración del perfil
 */
@Composable
fun ProfileOptions(
    ramOptimization: Boolean,
    cpuOptimization: Boolean,
    gpuOptimization: Boolean,
    batteryOptimization: Boolean,
    screenBrightness: Int,
    backgroundLimit: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileOption(
            label = "Optimización RAM",
            enabled = ramOptimization
        )
        ProfileOption(
            label = "Optimización CPU",
            enabled = cpuOptimization
        )
        if (gpuOptimization) {
            ProfileOption(
                label = "Optimización GPU",
                enabled = gpuOptimization
            )
        }
        if (batteryOptimization) {
            ProfileOption(
                label = "Ahorro de Batería",
                enabled = batteryOptimization
            )
        }
        if (backgroundLimit) {
            ProfileOption(
                label = "Limitar apps de fondo",
                enabled = backgroundLimit
            )
        }

        Text(
            text = "Brillo pantalla: $screenBrightness%",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileOption(
    label: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (enabled) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Habilitado",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.clickable { }
            )
        }
    }
}
