package any.mapper.ui.viewmodel

import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import any.mapper.data.model.Profile
import any.mapper.data.repository.MappingRepository
import any.mapper.input.ControllerDetector
import any.mapper.input.ControllerDevice
import any.mapper.service.MapperAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MappingRepository,
    private val controllerDetector: ControllerDetector,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val activeProfile: StateFlow<Profile?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<Profile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val connectedController: StateFlow<ControllerDevice?> = controllerDetector.controllers
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMappingCount: StateFlow<Int> = activeProfile
        .filterNotNull()
        .flatMapLatest { repository.getMappingsForProfile(it.id) }
        .map { it.count { m -> m.enabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun isAccessibilityEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(context.packageName, ignoreCase = true)
    }

    fun isServiceRunning(): Boolean = MapperAccessibilityService.instance != null

    fun toggleService() = MapperAccessibilityService.instance?.toggle()

    fun switchProfile(profileId: Long) {
        viewModelScope.launch { repository.switchActiveProfile(profileId) }
    }
}
