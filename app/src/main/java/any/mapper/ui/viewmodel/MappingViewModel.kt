package any.mapper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import any.mapper.data.model.*
import any.mapper.data.repository.MappingRepository
import any.mapper.service.MapperAccessibilityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MappingViewModel @Inject constructor(
    private val repository: MappingRepository
) : ViewModel() {

    val activeProfile: StateFlow<Profile?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProfiles: StateFlow<List<Profile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mappings: StateFlow<List<Mapping>> = activeProfile
        .filterNotNull()
        .flatMapLatest { repository.getMappingsForProfile(it.id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Detected input from service (for mapping editor)
    val detectedInput: SharedFlow<Pair<Int, SourceType>> = MapperAccessibilityService.detectedInputFlow

    fun insertMapping(mapping: Mapping) = viewModelScope.launch { repository.insertMapping(mapping) }
    fun updateMapping(mapping: Mapping) = viewModelScope.launch { repository.updateMapping(mapping) }
    fun deleteMapping(mapping: Mapping) = viewModelScope.launch { repository.deleteMapping(mapping) }
    fun setMappingEnabled(id: Long, enabled: Boolean) = viewModelScope.launch {
        repository.setMappingEnabled(id, enabled)
    }

    fun createProfile(name: String) = viewModelScope.launch { repository.createProfile(name) }
    fun deleteProfile(profile: Profile) = viewModelScope.launch { repository.deleteProfile(profile) }
    fun switchProfile(id: Long) = viewModelScope.launch { repository.switchActiveProfile(id) }
    fun updateProfile(profile: Profile) = viewModelScope.launch { repository.updateProfile(profile) }
    fun duplicateProfile(profileId: Long, newName: String) = viewModelScope.launch {
        repository.duplicateProfile(profileId, newName)
    }

    fun quickMapStickToMouse(rightStick: Boolean) = viewModelScope.launch {
        val profileId = activeProfile.value?.id ?: return@launch
        val axisX = if (rightStick) android.view.MotionEvent.AXIS_Z else android.view.MotionEvent.AXIS_X
        val axisY = if (rightStick) android.view.MotionEvent.AXIS_RZ else android.view.MotionEvent.AXIS_Y
        val label = if (rightStick) "Right Stick" else "Left Stick"
        repository.insertMapping(Mapping(profileId = profileId, label = "$label → Mouse X",
            sourceType = SourceType.AXIS_FULL, sourceCode = axisX,
            targetType = TargetType.MOUSE_MOVE_X, sensitivity = 2f))
        repository.insertMapping(Mapping(profileId = profileId, label = "$label → Mouse Y",
            sourceType = SourceType.AXIS_FULL, sourceCode = axisY,
            targetType = TargetType.MOUSE_MOVE_Y, sensitivity = 2f))
    }

    fun createQuickWinlatorProfile() = viewModelScope.launch {
        val mappings = listOf(
            Mapping(profileId = 0, label = "Left Stick Up → W", sourceType = SourceType.AXIS_POS, sourceCode = android.view.MotionEvent.AXIS_Y.inv(), targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_W),
            Mapping(profileId = 0, label = "Left Stick Down → S", sourceType = SourceType.AXIS_NEG, sourceCode = android.view.MotionEvent.AXIS_Y, targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_S),
            Mapping(profileId = 0, label = "Left Stick Left → A", sourceType = SourceType.AXIS_NEG, sourceCode = android.view.MotionEvent.AXIS_X, targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_A),
            Mapping(profileId = 0, label = "Left Stick Right → D", sourceType = SourceType.AXIS_POS, sourceCode = android.view.MotionEvent.AXIS_X, targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_D),
            Mapping(profileId = 0, label = "Right Stick → Mouse X", sourceType = SourceType.AXIS_FULL, sourceCode = android.view.MotionEvent.AXIS_Z, targetType = TargetType.MOUSE_MOVE_X, sensitivity = 2f),
            Mapping(profileId = 0, label = "Right Stick → Mouse Y", sourceType = SourceType.AXIS_FULL, sourceCode = android.view.MotionEvent.AXIS_RZ, targetType = TargetType.MOUSE_MOVE_Y, sensitivity = 2f),
            Mapping(profileId = 0, label = "A → Space / Jump", sourceType = SourceType.BUTTON, sourceCode = android.view.KeyEvent.KEYCODE_BUTTON_A, targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_SPACE),
            Mapping(profileId = 0, label = "B → Crouch (Ctrl)", sourceType = SourceType.BUTTON, sourceCode = android.view.KeyEvent.KEYCODE_BUTTON_B, targetType = TargetType.KEY, targetKeyCode = android.view.KeyEvent.KEYCODE_CTRL_LEFT),
            Mapping(profileId = 0, label = "LT → Left Click", sourceType = SourceType.BUTTON, sourceCode = android.view.KeyEvent.KEYCODE_BUTTON_L2, targetType = TargetType.MOUSE_LEFT),
            Mapping(profileId = 0, label = "RT → Right Click", sourceType = SourceType.BUTTON, sourceCode = android.view.KeyEvent.KEYCODE_BUTTON_R2, targetType = TargetType.MOUSE_RIGHT),
        )
        repository.createQuickSetupProfile("Winlator FPS", "com.winlator", mappings)
    }
}
