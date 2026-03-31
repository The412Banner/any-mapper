package any.mapper.input

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ControllerDevice(
    val id: Int,
    val name: String,
    val descriptor: String,
    val hasAnalog: Boolean,
    val isGamepad: Boolean
)

@Singleton
class ControllerDetector @Inject constructor(
    @ApplicationContext private val context: Context
) : InputManager.InputDeviceListener {

    private val inputManager = context.getSystemService(Context.INPUT_SERVICE) as InputManager
    private val _controllers = MutableStateFlow<List<ControllerDevice>>(emptyList())
    val controllers: StateFlow<List<ControllerDevice>> = _controllers.asStateFlow()

    init {
        inputManager.registerInputDeviceListener(this, null)
        refresh()
    }

    private fun refresh() {
        _controllers.value = InputDevice.getDeviceIds().toList()
            .mapNotNull { id -> InputDevice.getDevice(id) }
            .filter { device -> device.isGamepad() }
            .map { device -> device.toControllerDevice() }
    }

    private fun InputDevice.isGamepad(): Boolean {
        val sources = sources
        return (sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD) ||
               (sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK)
    }

    private fun InputDevice.toControllerDevice() = ControllerDevice(
        id = id,
        name = name ?: "Unknown Controller",
        descriptor = descriptor ?: "",
        hasAnalog = hasAnalogAxes(),
        isGamepad = sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
    )

    private fun InputDevice.hasAnalogAxes(): Boolean =
        getMotionRange(android.view.MotionEvent.AXIS_X) != null ||
        getMotionRange(android.view.MotionEvent.AXIS_Z) != null

    override fun onInputDeviceAdded(deviceId: Int) { refresh() }
    override fun onInputDeviceRemoved(deviceId: Int) { refresh() }
    override fun onInputDeviceChanged(deviceId: Int) { refresh() }

    val primaryController: ControllerDevice? get() = _controllers.value.firstOrNull()
}
