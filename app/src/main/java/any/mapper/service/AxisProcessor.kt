package any.mapper.service

import any.mapper.data.model.CurveType
import any.mapper.data.model.Mapping
import any.mapper.data.model.SourceType
import any.mapper.data.model.TargetType
import any.mapper.input.InputInjector
import android.view.KeyEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sign

sealed class AxisResult {
    data class Key(val keyCode: Int, val action: Int) : AxisResult()
    data class Mouse(val axis: Int, val value: Float) : AxisResult()
    data class MouseClick(val button: Int, val action: Int) : AxisResult()
}

@Singleton
class AxisProcessor @Inject constructor() {

    // Track key states per mapping ID to avoid spam
    private val keyStates = mutableMapOf<Long, Boolean>() // mappingId -> isDown

    fun process(axisId: Int, rawValue: Float, mapping: Mapping): AxisResult? {
        val processed = applyDeadZoneAndCurve(rawValue, mapping.deadZone, mapping.curve)
        val scaled = processed * mapping.sensitivity

        return when (mapping.sourceType) {
            SourceType.AXIS_FULL -> when (mapping.targetType) {
                TargetType.MOUSE_MOVE_X, TargetType.MOUSE_MOVE_Y ->
                    AxisResult.Mouse(axisId, scaled)
                TargetType.MOUSE_LEFT -> mouseClick(mapping, scaled)
                else -> null
            }
            SourceType.AXIS_POS -> if (processed > 0f) axisToKey(mapping, processed) else keyUp(mapping)
            SourceType.AXIS_NEG -> if (processed < 0f) axisToKey(mapping, abs(processed)) else keyUp(mapping)
            SourceType.BUTTON -> null // handled by onKeyEvent
        }
    }

    private fun applyDeadZoneAndCurve(value: Float, deadZone: Float, curve: CurveType): Float {
        if (abs(value) < deadZone) return 0f
        val normalized = (abs(value) - deadZone) / (1f - deadZone)
        val curved = when (curve) {
            CurveType.LINEAR -> normalized
            CurveType.QUADRATIC -> normalized * normalized
            CurveType.CUBIC -> normalized * normalized * normalized
        }
        return curved * value.sign
    }

    private fun axisToKey(mapping: Mapping, magnitude: Float): AxisResult? {
        val wasDown = keyStates[mapping.id] ?: false
        return if (!wasDown) {
            keyStates[mapping.id] = true
            AxisResult.Key(mapping.targetKeyCode, KeyEvent.ACTION_DOWN)
        } else null
    }

    private fun keyUp(mapping: Mapping): AxisResult? {
        val wasDown = keyStates[mapping.id] ?: false
        return if (wasDown) {
            keyStates[mapping.id] = false
            AxisResult.Key(mapping.targetKeyCode, KeyEvent.ACTION_UP)
        } else null
    }

    private fun mouseClick(mapping: Mapping, value: Float): AxisResult? {
        val wasDown = keyStates[mapping.id] ?: false
        val isDown = abs(value) > 0.5f
        return if (isDown && !wasDown) {
            keyStates[mapping.id] = true
            AxisResult.MouseClick(android.view.MotionEvent.BUTTON_PRIMARY, android.view.MotionEvent.ACTION_BUTTON_PRESS)
        } else if (!isDown && wasDown) {
            keyStates[mapping.id] = false
            AxisResult.MouseClick(android.view.MotionEvent.BUTTON_PRIMARY, android.view.MotionEvent.ACTION_BUTTON_RELEASE)
        } else null
    }

    fun reset() = keyStates.clear()
}
