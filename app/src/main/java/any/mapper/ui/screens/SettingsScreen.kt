package any.mapper.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import any.mapper.R
import any.mapper.ui.viewmodel.MappingViewModel

@Composable
fun SettingsScreen(vm: MappingViewModel = hiltViewModel()) {
    var mouseTouchMode by remember { mutableStateOf(true) }
    var defaultSensitivity by remember { mutableFloatStateOf(1f) }
    var defaultDeadzone by remember { mutableFloatStateOf(0.15f) }
    var startOnBoot by remember { mutableStateOf(false) }
    var vibrateOnMap by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { SectionLabel(stringResource(R.string.settings_mouse_section)) }

        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_mouse_mode), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.settings_mouse_mode_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = mouseTouchMode, onClick = { mouseTouchMode = true },
                            label = { Text(stringResource(R.string.settings_mouse_mode_touch)) })
                        FilterChip(selected = !mouseTouchMode, onClick = { mouseTouchMode = false },
                            label = { Text(stringResource(R.string.settings_mouse_mode_mouse)) })
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("${stringResource(R.string.settings_default_sensitivity)}: ${String.format("%.1f", defaultSensitivity)}")
                    Slider(value = defaultSensitivity, onValueChange = { defaultSensitivity = it }, valueRange = 0.1f..5f)
                    Spacer(Modifier.height(4.dp))
                    Text("${stringResource(R.string.settings_default_deadzone)}: ${String.format("%.2f", defaultDeadzone)}")
                    Slider(value = defaultDeadzone, onValueChange = { defaultDeadzone = it }, valueRange = 0f..0.5f)
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_service_section)) }
        item {
            Card {
                Column {
                    SettingsToggle(stringResource(R.string.settings_start_on_boot), startOnBoot) { startOnBoot = it }
                    HorizontalDivider()
                    SettingsToggle(stringResource(R.string.settings_vibrate_on_map), vibrateOnMap) { vibrateOnMap = it }
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_compat_section)) }
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.settings_compat_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    listOf(
                        stringResource(R.string.settings_compat_winlator) to { vm.createQuickWinlatorProfile() },
                    ).forEach { (label, action) ->
                        OutlinedButton(onClick = action, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        item { SectionLabel(stringResource(R.string.settings_about_section)) }
        item {
            Card {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_version, "1.0.0")) },
                        leadingContent = { Icon(Icons.Default.Info, null) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("GitHub") },
                        leadingContent = { Icon(Icons.Default.Code, null) },
                        supportingContent = { Text("github.com/The412Banner/any-mapper") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onCheck: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheck) }
    )
}
