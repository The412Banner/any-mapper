package any.mapper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import any.mapper.ui.AnyMapperNavGraph
import any.mapper.ui.screens.OnboardingScreen
import any.mapper.ui.theme.AnyMapperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val onboardingDone = runBlocking {
            dataStore.data.first()[ONBOARDING_COMPLETE] ?: false
        }

        setContent {
            AnyMapperTheme {
                var showOnboarding by remember { mutableStateOf(!onboardingDone) }
                if (showOnboarding) {
                    OnboardingScreen(onComplete = {
                        showOnboarding = false
                        runBlocking {
                            dataStore.updateData { prefs ->
                                prefs.toMutablePreferences().apply {
                                    set(ONBOARDING_COMPLETE, true)
                                }
                            }
                        }
                    })
                } else {
                    AnyMapperNavGraph()
                }
            }
        }
    }
}
