@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
package any.mapper.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import any.mapper.data.model.SourceType
import any.mapper.input.KeyCodeHelper
import any.mapper.service.MapperAccessibilityService
import any.mapper.ui.viewmodel.MappingViewModel
import kotlinx.coroutines.launch

@Composable
fun ControllerTestScreen(vm: MappingViewModel = hiltViewModel()) {
    val mappings by vm.mappings.collectAsState()
    val scope = rememberCoroutineScope()

    var lastDetectedKey by remember { mutableIntStateOf(-1) }
    var lastDetectedType by remember { mutableStateOf(SourceType.BUTTON) }

    LaunchedEffect(Unit) {
        MapperAccessibilityService.detectedInputFlow.collect { (code, type) ->
            lastDetectedKey = code
            lastDetectedType = type
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Live Input", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (lastDetectedKey == -1) {
                        Text(stringResource(R.string.test_no_controller),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val name = KeyCodeHelper.getSourceName(lastDetectedKey, lastDetectedType)
                        val mapping = mappings.firstOrNull { it.sourceCode == lastDetectedKey }
                        Text("Input: $name", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (mapping != null)
                                stringResource(R.string.test_mapped_to, KeyCodeHelper.getTargetName(mapping))
                            else stringResource(R.string.test_not_mapped),
                            color = if (mapping != null) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Button mappings visual
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.mappings_section_buttons),
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    if (mappings.filter { it.sourceType == SourceType.BUTTON }.isEmpty()) {
                        Text("No button mappings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        mappings.filter { it.sourceType == SourceType.BUTTON }.forEach { mapping ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val isActive = lastDetectedKey == mapping.sourceCode
                                Surface(
                                    color = if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        KeyCodeHelper.getSourceName(mapping.sourceCode, mapping.sourceType),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text("→", modifier = Modifier.padding(horizontal = 8.dp))
                                Text(KeyCodeHelper.getTargetName(mapping),
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    scope.launch { MapperAccessibilityService.instance?.let {
                        // reset cursor
                    }}
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CenterFocusStrong, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.test_reset_cursor))
            }
        }
    }
}
