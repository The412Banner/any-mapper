@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
package any.mapper.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import any.mapper.R
import any.mapper.data.model.*
import any.mapper.input.KeyCodeHelper
import any.mapper.service.MapperAccessibilityService
import any.mapper.ui.viewmodel.MappingViewModel
import kotlinx.coroutines.flow.first

@Composable
fun MappingEditorScreen(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    vm: MappingViewModel = hiltViewModel()
) {
    val activeProfile by vm.activeProfile.collectAsState()

    var step by remember { mutableIntStateOf(0) }
    var sourceCode by remember { mutableIntStateOf(0) }
    var sourceType by remember { mutableStateOf(SourceType.BUTTON) }
    var targetType by remember { mutableStateOf(TargetType.KEY) }
    var targetKeyCode by remember { mutableIntStateOf(0) }
    var targetModifiers by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var deadZone by remember { mutableFloatStateOf(0.15f) }
    var sensitivity by remember { mutableFloatStateOf(1.0f) }
    var curve by remember { mutableStateOf(CurveType.LINEAR) }
    var holdBehavior by remember { mutableStateOf(HoldType.HELD) }
    var selectedKeyGroup by remember { mutableStateOf("Letters") }

    // Listen for detected input from service
    LaunchedEffect(step) {
        if (step == 0) {
            MapperAccessibilityService.isListeningForInput = true
            val detected = vm.detectedInput.first()
            MapperAccessibilityService.isListeningForInput = false
            sourceCode = detected.first
            sourceType = detected.second
            step = 1
        }
    }

    DisposableEffect(Unit) {
        onDispose { MapperAccessibilityService.isListeningForInput = false }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title_new)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, null) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Detect source
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (step >= 1) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1", style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.editor_step1_title),
                                style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        if (step == 0) {
                            Text(stringResource(R.string.editor_step1_waiting),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                KeyCodeHelper.getSourceName(sourceCode, sourceType),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Step 2: Target
            if (step >= 1) {
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("2", style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.editor_step2_title),
                                    style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.height(12.dp))

                            // Target type tabs
                            val targetTabs = listOf("Key", "Mouse Left", "Mouse Right", "Mouse Middle", "Mouse X", "Mouse Y")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(targetTabs) { tab ->
                                    val tt = when (tab) {
                                        "Key" -> TargetType.KEY
                                        "Mouse Left" -> TargetType.MOUSE_LEFT
                                        "Mouse Right" -> TargetType.MOUSE_RIGHT
                                        "Mouse Middle" -> TargetType.MOUSE_MIDDLE
                                        "Mouse X" -> TargetType.MOUSE_MOVE_X
                                        "Mouse Y" -> TargetType.MOUSE_MOVE_Y
                                        else -> TargetType.KEY
                                    }
                                    FilterChip(
                                        selected = targetType == tt,
                                        onClick = { targetType = tt },
                                        label = { Text(tab) }
                                    )
                                }
                            }

                            if (targetType == TargetType.KEY) {
                                Spacer(Modifier.height(12.dp))
                                // Key group selector
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(KeyCodeHelper.keyboardKeysGrouped.keys.toList()) { group ->
                                        FilterChip(
                                            selected = selectedKeyGroup == group,
                                            onClick = { selectedKeyGroup = group },
                                            label = { Text(group) }
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                // Key grid
                                val keys = KeyCodeHelper.keyboardKeysGrouped[selectedKeyGroup] ?: emptyList()
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(keys) { (code, name) ->
                                        Surface(
                                            modifier = Modifier.clickable { targetKeyCode = code },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (targetKeyCode == code)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(name, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                color = if (targetKeyCode == code)
                                                    MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                if (targetKeyCode != 0) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Selected: ${KeyCodeHelper.keyboardKeyNames[targetKeyCode] ?: "Key $targetKeyCode"}",
                                        color = MaterialTheme.colorScheme.primary)
                                }
                                // Modifier checkboxes
                                Spacer(Modifier.height(8.dp))
                                Text("Modifiers:", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Ctrl" to android.view.KeyEvent.META_CTRL_ON,
                                           "Shift" to android.view.KeyEvent.META_SHIFT_ON,
                                           "Alt" to android.view.KeyEvent.META_ALT_ON).forEach { (name, flag) ->
                                        FilterChip(
                                            selected = targetModifiers and flag != 0,
                                            onClick = {
                                                targetModifiers = targetModifiers xor flag
                                                if (targetModifiers != 0) targetType = TargetType.KEY_COMBO
                                            },
                                            label = { Text(name) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Step 3: Configure
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("3", style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.editor_step3_title),
                                    style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = label,
                                onValueChange = { label = it },
                                label = { Text(stringResource(R.string.editor_label_hint)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (sourceType != SourceType.BUTTON) {
                                Spacer(Modifier.height(12.dp))
                                Text("${stringResource(R.string.editor_deadzone)}: ${String.format("%.2f", deadZone)}")
                                Slider(value = deadZone, onValueChange = { deadZone = it },
                                    valueRange = 0f..0.5f)
                                Spacer(Modifier.height(8.dp))
                                Text("${stringResource(R.string.editor_sensitivity)}: ${String.format("%.1f", sensitivity)}")
                                Slider(value = sensitivity, onValueChange = { sensitivity = it },
                                    valueRange = 0.1f..5f)
                                Spacer(Modifier.height(8.dp))
                                Text(stringResource(R.string.editor_curve))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CurveType.values().forEach { c ->
                                        FilterChip(selected = curve == c, onClick = { curve = c },
                                            label = { Text(c.name.lowercase().replaceFirstChar { it.uppercase() }) })
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.editor_hold_behavior))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HoldType.values().forEach { h ->
                                    FilterChip(selected = holdBehavior == h, onClick = { holdBehavior = h },
                                        label = { Text(when(h) {
                                            HoldType.HELD -> stringResource(R.string.editor_hold_held)
                                            HoldType.TOGGLE -> stringResource(R.string.editor_hold_toggle)
                                            HoldType.SINGLE_SHOT -> stringResource(R.string.editor_hold_single)
                                        }) })
                                }
                            }
                        }
                    }
                }

                item {
                    val canSave = (targetType != TargetType.KEY && targetType != TargetType.KEY_COMBO) || targetKeyCode != 0
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.editor_cancel))
                        }
                        Button(
                            onClick = {
                                activeProfile?.let { profile ->
                                    val mapping = Mapping(
                                        profileId = profile.id,
                                        label = label,
                                        sourceType = sourceType,
                                        sourceCode = sourceCode,
                                        targetType = targetType,
                                        targetKeyCode = targetKeyCode,
                                        targetModifiers = targetModifiers,
                                        deadZone = deadZone,
                                        sensitivity = sensitivity,
                                        curve = curve,
                                        holdBehavior = holdBehavior
                                    )
                                    vm.insertMapping(mapping)
                                    onSave()
                                }
                            },
                            enabled = canSave,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.editor_save))
                        }
                    }
                }
            }
        }
    }
}
