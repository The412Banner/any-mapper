package any.mapper.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import any.mapper.MainActivity
import any.mapper.R
import any.mapper.data.model.Mapping
import any.mapper.data.model.SourceType
import any.mapper.data.model.TargetType
import any.mapper.data.repository.MappingRepository
import any.mapper.input.InputInjector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

@AndroidEntryPoint
class MapperAccessibilityService : AccessibilityService() {

    @Inject lateinit var repository: MappingRepository
    @Inject lateinit var injector: InputInjector
    @Inject lateinit var axisProcessor: AxisProcessor
    @Inject lateinit var mouseCursorManager: MouseCursorManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var activeMappings: List<Mapping> = emptyList()
    private var isEnabled = true
    private val toggleStates = mutableMapOf<Long, Boolean>()

    companion object {
        var instance: MapperAccessibilityService? = null
        val detectedInputFlow = MutableSharedFlow<Pair<Int, SourceType>>(extraBufferCapacity = 1)
        const val ACTION_TOGGLE = "any.mapper.ACTION_TOGGLE"
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "mapper_service"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        setupNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        loadMappings()
        mouseCursorManager.start()
    }

    private fun loadMappings() {
        scope.launch {
            repository.activeProfile.collect { profile ->
                activeMappings = if (profile != null) {
                    repository.getEnabledMappingsForProfile(profile.id)
                } else emptyList()
                withContext(Dispatchers.Main) {
                    updateNotification(profile?.name)
                }
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!isEnabled) return false

        // Broadcast for mapping editor detection
        scope.launch {
            detectedInputFlow.emit(event.keyCode to SourceType.BUTTON)
        }

        val mapping = activeMappings.firstOrNull {
            it.sourceType == SourceType.BUTTON && it.sourceCode == event.keyCode
        } ?: return false

        when (mapping.targetType) {
            TargetType.KEY -> {
                handleKeyMapping(mapping, event)
                return true
            }
            TargetType.KEY_COMBO -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val mods = buildModifierList(mapping.targetModifiers)
                    injector.injectKeyCombo(mods, mapping.targetKeyCode)
                }
                return true
            }
            TargetType.MOUSE_LEFT -> {
                if (event.action == KeyEvent.ACTION_DOWN) injector.injectMouseButtonDown()
                else injector.injectMouseButtonUp()
                return true
            }
            TargetType.MOUSE_RIGHT -> {
                if (event.action == KeyEvent.ACTION_DOWN)
                    injector.injectMouseButtonDown(MotionEvent.BUTTON_SECONDARY)
                else injector.injectMouseButtonUp(MotionEvent.BUTTON_SECONDARY)
                return true
            }
            TargetType.MOUSE_MIDDLE -> {
                if (event.action == KeyEvent.ACTION_DOWN)
                    injector.injectMouseButtonDown(MotionEvent.BUTTON_TERTIARY)
                else injector.injectMouseButtonUp(MotionEvent.BUTTON_TERTIARY)
                return true
            }
            else -> return false
        }
    }

    private fun handleKeyMapping(mapping: Mapping, event: KeyEvent) {
        when (mapping.holdBehavior) {
            any.mapper.data.model.HoldType.HELD -> {
                injector.injectKey(mapping.targetKeyCode, event.action, mapping.targetModifiers)
            }
            any.mapper.data.model.HoldType.TOGGLE -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val isDown = toggleStates[mapping.id] ?: false
                    if (isDown) {
                        injector.injectKeyUp(mapping.targetKeyCode, mapping.targetModifiers)
                    } else {
                        injector.injectKeyDown(mapping.targetKeyCode, mapping.targetModifiers)
                    }
                    toggleStates[mapping.id] = !isDown
                }
            }
            any.mapper.data.model.HoldType.SINGLE_SHOT -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    injector.injectKeyDown(mapping.targetKeyCode, mapping.targetModifiers)
                    injector.injectKeyUp(mapping.targetKeyCode, mapping.targetModifiers)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMotionEvent(event: MotionEvent) {
        if (!isEnabled) return

        val axisMappings = activeMappings.filter {
            it.sourceType != SourceType.BUTTON
        }

        var mouseAxisX = 0f
        var mouseAxisY = 0f
        var mouseSensitivity = 1f

        val relevantAxes = listOf(
            MotionEvent.AXIS_X, MotionEvent.AXIS_Y,
            MotionEvent.AXIS_Z, MotionEvent.AXIS_RZ,
            MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_RTRIGGER,
            MotionEvent.AXIS_HAT_X, MotionEvent.AXIS_HAT_Y
        )

        relevantAxes.forEach { axis ->
            val value = event.getAxisValue(axis)
            axisMappings.filter { it.sourceCode == axis }.forEach { mapping ->
                val result = axisProcessor.process(axis, value, mapping) ?: return@forEach
                when (result) {
                    is AxisResult.Key -> injector.injectKey(result.keyCode, result.action)
                    is AxisResult.Mouse -> {
                        when (mapping.targetType) {
                            TargetType.MOUSE_MOVE_X -> { mouseAxisX = result.value; mouseSensitivity = mapping.sensitivity }
                            TargetType.MOUSE_MOVE_Y -> { mouseAxisY = result.value; mouseSensitivity = mapping.sensitivity }
                            else -> {}
                        }
                    }
                    is AxisResult.MouseClick -> injector.injectMouseButtonDown(result.button)
                }
            }
        }

        if (mouseAxisX != 0f || mouseAxisY != 0f) {
            mouseCursorManager.updateAxis(mouseAxisX, mouseAxisY, mouseSensitivity)
        } else {
            mouseCursorManager.updateAxis(0f, 0f, 1f)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            scope.launch {
                val profile = repository.getProfileForPackage(pkg) ?: return@launch
                val current = repository.getActiveProfileOnce()
                if (profile.id != current?.id) {
                    repository.switchActiveProfile(profile.id)
                }
            }
        }
    }

    override fun onInterrupt() {}

    fun toggle() {
        isEnabled = !isEnabled
        if (!isEnabled) axisProcessor.reset()
        updateNotification(null)
    }

    private fun buildModifierList(modifiers: Int): List<Int> = buildList {
        if (modifiers and KeyEvent.META_CTRL_ON != 0) add(KeyEvent.KEYCODE_CTRL_LEFT)
        if (modifiers and KeyEvent.META_SHIFT_ON != 0) add(KeyEvent.KEYCODE_SHIFT_LEFT)
        if (modifiers and KeyEvent.META_ALT_ON != 0) add(KeyEvent.KEYCODE_ALT_LEFT)
        if (modifiers and KeyEvent.META_META_ON != 0) add(KeyEvent.KEYCODE_META_LEFT)
    }

    private fun setupNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Mapper Service", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Any Mapper background service status" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(profileName: String? = null): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val toggleIntent = Intent(this, MapperAccessibilityService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePi = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_IMMUTABLE)

        val statusText = if (isEnabled)
            "Active${if (profileName != null) " — $profileName" else ""}"
        else "Disabled"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Any Mapper")
            .setContentText(statusText)
            .setContentIntent(pi)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isEnabled) "Disable" else "Enable",
                togglePi
            )
            .build()
    }

    private fun updateNotification(profileName: String?) {
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(profileName))
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
        mouseCursorManager.stop()
    }
}
