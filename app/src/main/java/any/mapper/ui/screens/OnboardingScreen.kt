@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.animation.ExperimentalAnimationApi::class)
package any.mapper.ui.screens


import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
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
import any.mapper.R

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val needsRestrictedStep = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val totalSteps = if (needsRestrictedStep) 4 else 3
    var step by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        AnimatedContent(targetState = step, label = "onboarding") { s ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    s == 0 -> {
                        Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.onboarding_title_1),
                            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.onboarding_desc_1),
                            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    needsRestrictedStep && s == 1 -> {
                        Icon(Icons.Default.Security, null, modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.onboarding_title_restricted),
                            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.onboarding_desc_restricted),
                            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = {
                            val uri = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        }) {
                            Icon(Icons.Default.OpenInNew, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.onboarding_open_app_info))
                        }
                    }
                    (!needsRestrictedStep && s == 1) || (needsRestrictedStep && s == 2) -> {
                        Icon(Icons.Default.Accessibility, null, modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.onboarding_title_2),
                            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.onboarding_desc_2),
                            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            })
                        }) {
                            Icon(Icons.Default.OpenInNew, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.onboarding_grant))
                        }
                    }
                    else -> {
                        Icon(Icons.Default.Gamepad, null, modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.onboarding_title_3),
                            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Text(stringResource(R.string.onboarding_desc_3),
                            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(totalSteps) { i ->
                    Box(modifier = Modifier.size(if (i == step) 12.dp else 8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.small,
                            color = if (i == step) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        ) {}
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onComplete) { Text(stringResource(R.string.onboarding_skip)) }
                Button(onClick = { if (step < totalSteps - 1) step++ else onComplete() }) {
                    Text(if (step < totalSteps - 1) stringResource(R.string.onboarding_next) else stringResource(R.string.onboarding_finish))
                }
            }
        }
    }
}
