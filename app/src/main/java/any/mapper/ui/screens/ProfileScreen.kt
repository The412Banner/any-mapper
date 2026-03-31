@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
package any.mapper.ui.screens


import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import any.mapper.data.model.Profile
import any.mapper.ui.viewmodel.MappingViewModel

data class AppInfo(val label: String, val packageName: String)

@Composable
fun ProfileScreen(vm: MappingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val profiles by vm.allProfiles.collectAsState()
    val activeProfile by vm.activeProfile.collectAsState()

    var showNewDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var deletingProfile by remember { mutableStateOf<Profile?>(null) }
    var newName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var showFabMenu by remember { mutableStateOf(false) }
    var importError by remember { mutableStateOf<String?>(null) }

    // ICP import state
    var pendingIcpJson by remember { mutableStateOf<String?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var appPickerSearch by remember { mutableStateOf("") }

    // Load installed launchable apps lazily when picker is shown
    val installedApps = remember(showAppPicker) {
        if (!showAppPicker) emptyList()
        else {
            val pm = context.packageManager
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
                addCategory(android.content.Intent.CATEGORY_LAUNCHER)
            }
            pm.queryIntentActivities(intent, 0)
                .map { ri ->
                    AppInfo(
                        label = ri.loadLabel(pm).toString(),
                        packageName = ri.activityInfo.packageName
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }
        }
    }

    val icpPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.readText() ?: return@rememberLauncherForActivityResult
                pendingIcpJson = json
                appPickerSearch = ""
                showAppPicker = true
                showFabMenu = false
            } catch (e: Exception) {
                importError = e.message ?: "Failed to read file"
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showFabMenu) {
                    SmallFloatingActionButton(
                        onClick = { icpPicker.launch("*/*") },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.FileOpen, null, modifier = Modifier.size(18.dp))
                            Text("Import .icp file", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SmallFloatingActionButton(
                        onClick = { newName = ""; showNewDialog = true; showFabMenu = false },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Text("New profile", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                ExtendedFloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    icon = { Icon(if (showFabMenu) Icons.Default.Close else Icons.Default.Add, null) },
                    text = { Text(if (showFabMenu) "Close" else stringResource(R.string.profiles_new)) }
                )
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.profiles_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    ProfileRow(
                        profile = profile,
                        isActive = profile.id == activeProfile?.id,
                        onActivate = { vm.switchProfile(profile.id) },
                        onEdit = { editingProfile = profile; newName = profile.name; packageName = profile.autoActivatePackage ?: "" },
                        onDuplicate = { vm.duplicateProfile(profile.id, "${profile.name} (copy)") },
                        onDelete = { deletingProfile = profile }
                    )
                }
            }
        }
    }

    // App picker dialog after ICP file is loaded
    if (showAppPicker) {
        val filtered = installedApps.filter {
            appPickerSearch.isBlank() ||
            it.label.contains(appPickerSearch, ignoreCase = true) ||
            it.packageName.contains(appPickerSearch, ignoreCase = true)
        }
        AlertDialog(
            onDismissRequest = { showAppPicker = false; pendingIcpJson = null },
            title = { Text("Select app to activate for") },
            text = {
                Column {
                    OutlinedTextField(
                        value = appPickerSearch,
                        onValueChange = { appPickerSearch = it },
                        label = { Text("Search") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(filtered) { app ->
                            ListItem(
                                headlineContent = { Text(app.label) },
                                supportingContent = {
                                    Text(app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                modifier = Modifier.fillMaxWidth().clickable {
                                    pendingIcpJson?.let { vm.importFromIcp(it, app.packageName) }
                                    showAppPicker = false
                                    pendingIcpJson = null
                                }
                            )
                            HorizontalDivider()
                        }
                        item {
                            TextButton(
                                onClick = {
                                    pendingIcpJson?.let { vm.importFromIcp(it, null) }
                                    showAppPicker = false; pendingIcpJson = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Skip — no auto-activate") }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAppPicker = false; pendingIcpJson = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showNewDialog) {
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text(stringResource(R.string.profiles_new)) },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.profiles_name_hint)) },
                    singleLine = true)
            },
            confirmButton = {
                Button(onClick = { if (newName.isNotBlank()) { vm.createProfile(newName); showNewDialog = false } }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = { TextButton(onClick = { showNewDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    editingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { editingProfile = null },
            title = { Text(stringResource(R.string.profiles_set_package)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.profiles_name_hint)) }, singleLine = true)
                    OutlinedTextField(value = packageName, onValueChange = { packageName = it },
                        label = { Text(stringResource(R.string.profiles_package_hint)) }, singleLine = true)
                    Text("Common packages:", style = MaterialTheme.typography.labelSmall)
                    listOf(
                        "Winlator" to "com.winlator",
                        "Winlator (Ludashi)" to "com.ludashi.benchmark",
                        "BannerHub" to "banner.hub",
                        "GHL" to "gamehub.lite"
                    ).forEach { (name, pkg) ->
                        FilterChip(selected = packageName == pkg, onClick = { packageName = pkg },
                            label = { Text("$name ($pkg)") })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateProfile(profile.copy(
                        name = newName.ifBlank { profile.name },
                        autoActivatePackage = packageName.ifBlank { null }
                    ))
                    editingProfile = null
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { editingProfile = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    deletingProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { deletingProfile = null },
            title = { Text(stringResource(R.string.profiles_delete)) },
            text = { Text(stringResource(R.string.profiles_delete_confirm, profile.name)) },
            confirmButton = {
                Button(onClick = { vm.deleteProfile(profile); deletingProfile = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = { TextButton(onClick = { deletingProfile = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    importError?.let { err ->
        AlertDialog(
            onDismissRequest = { importError = null },
            title = { Text("Import failed") },
            text = { Text(err) },
            confirmButton = { TextButton(onClick = { importError = null }) { Text(stringResource(R.string.ok)) } }
        )
    }
}

@Composable
private fun ProfileRow(
    profile: Profile,
    isActive: Boolean,
    onActivate: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(profile.name) },
        supportingContent = {
            profile.autoActivatePackage?.let {
                Text(stringResource(R.string.profiles_auto_activate, it),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        leadingContent = {
            if (isActive)
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            else
                Icon(Icons.Default.RadioButtonUnchecked, null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    if (!isActive) DropdownMenuItem(
                        text = { Text(stringResource(R.string.profiles_active)) },
                        leadingIcon = { Icon(Icons.Default.Check, null) },
                        onClick = { onActivate(); showMenu = false }
                    )
                    DropdownMenuItem(text = { Text(stringResource(R.string.rename)) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { onEdit(); showMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.duplicate)) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null) },
                        onClick = { onDuplicate(); showMenu = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) },
                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                        onClick = { onDelete(); showMenu = false })
                }
            }
        }
    )
    HorizontalDivider()
}
