package any.mapper.data.icpimport

import android.view.KeyEvent
import android.view.MotionEvent
import any.mapper.data.model.*
import org.json.JSONObject

object IcpImporter {

    // Standard gamepad button assignment order for ICP BUTTON elements
    private val BUTTON_ORDER = listOf(
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_THUMBL,
        KeyEvent.KEYCODE_BUTTON_THUMBR,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_MODE,
    )

    data class ImportResult(
        val profileName: String,
        val mappings: List<Mapping>
    )

    fun parse(json: String): ImportResult {
        val root = JSONObject(json)
        val profileName = root.optString("name", "Imported Profile")
        val elements = root.getJSONArray("elements")
        val mappings = mutableListOf<Mapping>()
        var buttonIndex = 0

        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            val type = el.optString("type")
            val label = el.optString("text", type)
            val bindingsArr = el.getJSONArray("bindings")
            val bindings = (0 until bindingsArr.length()).map { bindingsArr.getString(it) }

            when (type) {
                "STICK" -> {
                    // bindings: [UP, RIGHT, DOWN, LEFT]
                    // Map to left stick thresholds (AXIS_X, AXIS_Y)
                    val up    = icpKeyToMapping(bindings.getOrNull(0), SourceType.AXIS_NEG, MotionEvent.AXIS_Y, "$label Up")
                    val right = icpKeyToMapping(bindings.getOrNull(1), SourceType.AXIS_POS, MotionEvent.AXIS_X, "$label Right")
                    val down  = icpKeyToMapping(bindings.getOrNull(2), SourceType.AXIS_POS, MotionEvent.AXIS_Y, "$label Down")
                    val left  = icpKeyToMapping(bindings.getOrNull(3), SourceType.AXIS_NEG, MotionEvent.AXIS_X, "$label Left")
                    listOfNotNull(up, right, down, left).forEach { mappings.add(it) }
                }
                "D_PAD" -> {
                    // bindings: [UP, RIGHT, DOWN, LEFT]
                    val up    = icpKeyToMapping(bindings.getOrNull(0), SourceType.BUTTON, KeyEvent.KEYCODE_DPAD_UP, "$label Up")
                    val right = icpKeyToMapping(bindings.getOrNull(1), SourceType.BUTTON, KeyEvent.KEYCODE_DPAD_RIGHT, "$label Right")
                    val down  = icpKeyToMapping(bindings.getOrNull(2), SourceType.BUTTON, KeyEvent.KEYCODE_DPAD_DOWN, "$label Down")
                    val left  = icpKeyToMapping(bindings.getOrNull(3), SourceType.BUTTON, KeyEvent.KEYCODE_DPAD_LEFT, "$label Left")
                    listOfNotNull(up, right, down, left).forEach { mappings.add(it) }
                }
                "BUTTON" -> {
                    val binding = bindings.firstOrNull { it != "NONE" }
                    if (binding != null) {
                        val sourceCode = BUTTON_ORDER.getOrElse(buttonIndex) { KeyEvent.KEYCODE_BUTTON_A }
                        buttonIndex++
                        val m = icpBindingToMapping(binding, SourceType.BUTTON, sourceCode, label)
                        if (m != null) mappings.add(m)
                    }
                }
            }
        }

        return ImportResult(profileName, mappings)
    }

    private fun icpKeyToMapping(
        binding: String?,
        sourceType: SourceType,
        sourceCode: Int,
        label: String
    ): Mapping? {
        if (binding == null || binding == "NONE") return null
        return icpBindingToMapping(binding, sourceType, sourceCode, label)
    }

    private fun icpBindingToMapping(
        binding: String,
        sourceType: SourceType,
        sourceCode: Int,
        label: String
    ): Mapping? {
        return when {
            binding == "MOUSE_LEFT_BUTTON" -> Mapping(
                profileId = 0, label = label,
                sourceType = sourceType, sourceCode = sourceCode,
                targetType = TargetType.MOUSE_LEFT
            )
            binding == "MOUSE_RIGHT_BUTTON" -> Mapping(
                profileId = 0, label = label,
                sourceType = sourceType, sourceCode = sourceCode,
                targetType = TargetType.MOUSE_RIGHT
            )
            binding == "MOUSE_MIDDLE_BUTTON" -> Mapping(
                profileId = 0, label = label,
                sourceType = sourceType, sourceCode = sourceCode,
                targetType = TargetType.MOUSE_MIDDLE
            )
            binding.startsWith("KEY_") -> {
                val keyCode = icpKeyNameToKeyCode(binding) ?: return null
                Mapping(
                    profileId = 0, label = label,
                    sourceType = sourceType, sourceCode = sourceCode,
                    targetType = TargetType.KEY, targetKeyCode = keyCode
                )
            }
            else -> null
        }
    }

    private fun icpKeyNameToKeyCode(name: String): Int? {
        val special = mapOf(
            "KEY_SPACE"   to KeyEvent.KEYCODE_SPACE,
            "KEY_ENTER"   to KeyEvent.KEYCODE_ENTER,
            "KEY_BKSP"    to KeyEvent.KEYCODE_DEL,
            "KEY_TAB"     to KeyEvent.KEYCODE_TAB,
            "KEY_ESC"     to KeyEvent.KEYCODE_ESCAPE,
            "KEY_SHIFT_L" to KeyEvent.KEYCODE_SHIFT_LEFT,
            "KEY_SHIFT_R" to KeyEvent.KEYCODE_SHIFT_RIGHT,
            "KEY_CTRL_L"  to KeyEvent.KEYCODE_CTRL_LEFT,
            "KEY_CTRL_R"  to KeyEvent.KEYCODE_CTRL_RIGHT,
            "KEY_ALT_L"   to KeyEvent.KEYCODE_ALT_LEFT,
            "KEY_ALT_R"   to KeyEvent.KEYCODE_ALT_RIGHT,
            "KEY_UP"      to KeyEvent.KEYCODE_DPAD_UP,
            "KEY_DOWN"    to KeyEvent.KEYCODE_DPAD_DOWN,
            "KEY_LEFT"    to KeyEvent.KEYCODE_DPAD_LEFT,
            "KEY_RIGHT"   to KeyEvent.KEYCODE_DPAD_RIGHT,
            "KEY_DEL"     to KeyEvent.KEYCODE_FORWARD_DEL,
            "KEY_HOME"    to KeyEvent.KEYCODE_MOVE_HOME,
            "KEY_END"     to KeyEvent.KEYCODE_MOVE_END,
            "KEY_PGUP"    to KeyEvent.KEYCODE_PAGE_UP,
            "KEY_PGDN"    to KeyEvent.KEYCODE_PAGE_DOWN,
            "KEY_INSERT"  to KeyEvent.KEYCODE_INSERT,
            "KEY_F1"  to KeyEvent.KEYCODE_F1,  "KEY_F2"  to KeyEvent.KEYCODE_F2,
            "KEY_F3"  to KeyEvent.KEYCODE_F3,  "KEY_F4"  to KeyEvent.KEYCODE_F4,
            "KEY_F5"  to KeyEvent.KEYCODE_F5,  "KEY_F6"  to KeyEvent.KEYCODE_F6,
            "KEY_F7"  to KeyEvent.KEYCODE_F7,  "KEY_F8"  to KeyEvent.KEYCODE_F8,
            "KEY_F9"  to KeyEvent.KEYCODE_F9,  "KEY_F10" to KeyEvent.KEYCODE_F10,
            "KEY_F11" to KeyEvent.KEYCODE_F11, "KEY_F12" to KeyEvent.KEYCODE_F12,
        )
        special[name]?.let { return it }

        // Single letter/digit: KEY_W → KEYCODE_W
        val stripped = name.removePrefix("KEY_")
        if (stripped.length == 1) {
            val kc = KeyEvent.keyCodeFromString("KEYCODE_$stripped")
            if (kc != KeyEvent.KEYCODE_UNKNOWN) return kc
        }
        // Digit: KEY_0..KEY_9
        val digitKc = KeyEvent.keyCodeFromString("KEYCODE_$stripped")
        if (digitKc != KeyEvent.KEYCODE_UNKNOWN) return digitKc

        return null
    }
}

