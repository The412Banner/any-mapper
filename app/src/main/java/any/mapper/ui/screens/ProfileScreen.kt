@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
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
import any.mapper.data.model.Profile
import any.mapper.ui.viewmodel.MappingViewModel

@Composable
fun ProfileScreen(vm: MappingViewModel = hiltViewModel()) {
    val profiles by vm.allProfiles.collectAsState()
    val activeProfile by vm.activeProfile.collectAsState()

    var showNewDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var deletingProfile by remember { mutableStateOf<Profile?>(null) }
    var newName by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { newName = ""; showNewDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.profiles_new)) }
            )
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
                        onEdit = { editingProfile = profile; packageName = profile.autoActivatePackage ?: "" },
                        onDuplicate = { vm.duplicateProfile(profile.id, "${profile.name} (copy)") },
                        onDelete = { deletingProfile = profile }
                    )
                }
            }
        }
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
                    listOf("Winlator" to "com.winlator",
                           "BannerHub" to "banner.hub",
                           "GHL" to "gamehub.lite").forEach { (name, pkg) ->
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
