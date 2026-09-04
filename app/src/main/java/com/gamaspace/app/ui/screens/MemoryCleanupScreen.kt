package com.gamaspace.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.gamaspace.app.ui.viewmodel.MemoryCleanupViewModel

/**
 * Pantalla de Limpieza de Memoria
 * Permite escanear y limpiar caché de aplicaciones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCleanupScreen(
    viewModel: MemoryCleanupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Limpieza de Memoria") },
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
                .padding(16.dp)
        ) {
            // Información de caché total
            CacheSummaryCard(
                totalSize = state.totalCacheSize,
                itemCount = state.cacheDataList.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.cleanSelectableCache() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !state.isCleaning
                ) {
                    Text("Limpiar Todo")
                }

                Button(
                    onClick = { viewModel.cleanSystemTemp() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = !state.isCleaning
                ) {
                    Text("Temp")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de éxito
            if (state.successMessage != null) {
                SuccessMessageCard(message = state.successMessage!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Mensaje de error
            if (state.error != null) {
                ErrorMessageCard(message = state.error!!)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Lista de caché
            if (state.cacheDataList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay datos de caché",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Text(
                    text = "Aplicaciones (${state.cacheDataList.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cacheDataList) { cacheData ->
                        CacheItemCard(
                            appName = cacheData.packageName,
                            cacheSize = cacheData.cacheSize,
                            onCleanClick = {
                                viewModel.cleanAppCache(cacheData.packageName)
                            },
                            isCleaning = state.isCleaning
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta resumen de caché total
 */
@Composable
fun CacheSummaryCard(totalSize: Long, itemCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Caché Total",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatBytes(totalSize),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$itemCount aplicación(es) con caché",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Tarjeta para cada aplicación con caché
 */
@Composable
fun CacheItemCard(
    appName: String,
    cacheSize: Long,
    onCleanClick: () -> Unit,
    isCleaning: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = appName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Caché: ${formatBytes(cacheSize)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onCleanClick,
                enabled = !isCleaning
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Limpiar",
                    tint = Color(0xFFFF9800)
                )
            }
        }
    }
}

/**
 * Tarjeta de mensaje de éxito
 */
@Composable
fun SuccessMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF2E7D32)
            )
        }
    }
}

/**
 * Tarjeta de mensaje de error
 */
@Composable
fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFEBEE))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFFC62828)
            )
        }
    }
}

/**
 * Formatea bytes a formato legible
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1073741824 -> String.format("%.2f GB", bytes / 1073741824.0)
        bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576.0)
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
