package any.mapper.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import any.mapper.R
import any.mapper.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(vm: HomeViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val activeProfile by vm.activeProfile.collectAsState()
    val allProfiles by vm.allProfiles.collectAsState()
    val controller by vm.connectedController.collectAsState()
    val mappingCount by vm.activeMappingCount.collectAsState()

    var showProfilePicker by remember { mutableStateOf(false) }
    var serviceRunning by remember { mutableStateOf(vm.isServiceRunning()) }
    val accessibilityEnabled = remember { vm.isAccessibilityEnabled() }

    LaunchedEffect(Unit) {
        serviceRunning = vm.isServiceRunning()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Accessibility warning
        if (!accessibilityEnabled) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.home_accessibility_off),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                    }) { Text(stringResource(R.string.home_grant_permission)) }
                }
            }
        }

        // Android 13 analog warning
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.home_analog_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }

        // Main service toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (serviceRunning)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (serviceRunning) Icons.Default.PlayCircle else Icons.Default.PauseCircle,
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = if (serviceRunning) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    if (serviceRunning) stringResource(R.string.home_service_enabled)
                    else stringResource(R.string.home_service_disabled),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (serviceRunning) stringResource(R.string.home_tap_to_disable)
                    else stringResource(R.string.home_tap_to_enable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        vm.toggleService()
                        serviceRunning = vm.isServiceRunning()
                    },
                    enabled = accessibilityEnabled
                ) {
                    Text(if (serviceRunning) stringResource(R.string.disable) else stringResource(R.string.enable))
                }
            }
        }

        // Active profile
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(stringResource(R.string.home_active_profile),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(activeProfile?.name ?: "No profile",
                        style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.home_mappings_active, mappingCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (allProfiles.size > 1) {
                    TextButton(onClick = { showProfilePicker = true }) {
                        Text("Switch")
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                }
            }
        }

        // Controller status
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Gamepad, null,
                    tint = if (controller != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    controller?.name ?: stringResource(R.string.home_no_controller),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    if (showProfilePicker) {
        AlertDialog(
            onDismissRequest = { showProfilePicker = false },
            title = { Text(stringResource(R.string.home_active_profile)) },
            text = {
                Column {
                    allProfiles.forEach { profile ->
                        ListItem(
                            headlineContent = { Text(profile.name) },
                            leadingContent = {
                                if (profile.isActive)
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                else
                                    Spacer(Modifier.size(24.dp))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfilePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
