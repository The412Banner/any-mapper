package any.mapper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import any.mapper.R
import any.mapper.data.model.Mapping
import any.mapper.data.model.SourceType
import any.mapper.input.KeyCodeHelper
import any.mapper.ui.viewmodel.MappingViewModel

@Composable
fun MappingListScreen(
    onAddMapping: () -> Unit,
    vm: MappingViewModel = hiltViewModel()
) {
    val mappings by vm.mappings.collectAsState()
    var deletingMapping by remember { mutableStateOf<Mapping?>(null) }

    val buttonMappings = mappings.filter { it.sourceType == SourceType.BUTTON }
    val axisMappings = mappings.filter { it.sourceType != SourceType.BUTTON }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMapping,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.mappings_add)) }
            )
        }
    ) { padding ->
        if (mappings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.mappings_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (buttonMappings.isNotEmpty()) {
                    item {
                        SectionHeader(stringResource(R.string.mappings_section_buttons))
                    }
                    items(buttonMappings, key = { it.id }) { mapping ->
                        MappingRow(
                            mapping = mapping,
                            onToggle = { vm.setMappingEnabled(mapping.id, !mapping.enabled) },
                            onDelete = { deletingMapping = mapping }
                        )
                    }
                }
                if (axisMappings.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.mappings_section_axes)) }
                    items(axisMappings, key = { it.id }) { mapping ->
                        MappingRow(
                            mapping = mapping,
                            onToggle = { vm.setMappingEnabled(mapping.id, !mapping.enabled) },
                            onDelete = { deletingMapping = mapping }
                        )
                    }
                }
            }
        }
    }

    deletingMapping?.let { mapping ->
        AlertDialog(
            onDismissRequest = { deletingMapping = null },
            title = { Text(stringResource(R.string.mappings_delete_confirm)) },
            text = { Text("${KeyCodeHelper.getSourceName(mapping.sourceCode, mapping.sourceType)} → ${KeyCodeHelper.getTargetName(mapping)}") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteMapping(mapping); deletingMapping = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingMapping = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun MappingRow(mapping: Mapping, onToggle: () -> Unit, onDelete: () -> Unit) {
    val sourceName = KeyCodeHelper.getSourceName(mapping.sourceCode, mapping.sourceType)
    val targetName = KeyCodeHelper.getTargetName(mapping)
    val label = mapping.label.ifBlank { "$sourceName → $targetName" }

    ListItem(
        headlineContent = { Text(label) },
        supportingContent = {
            Text("$sourceName  →  $targetName",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = mapping.enabled, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
    HorizontalDivider()
}
