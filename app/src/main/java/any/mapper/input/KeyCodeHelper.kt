package any.mapper.input

import android.view.KeyEvent
import android.view.MotionEvent

object KeyCodeHelper {

    val controllerButtonNames: Map<Int, String> = mapOf(
        KeyEvent.KEYCODE_BUTTON_A to "A Button",
        KeyEvent.KEYCODE_BUTTON_B to "B Button",
        KeyEvent.KEYCODE_BUTTON_X to "X Button",
        KeyEvent.KEYCODE_BUTTON_Y to "Y Button",
        KeyEvent.KEYCODE_BUTTON_L1 to "LB (Left Bumper)",
        KeyEvent.KEYCODE_BUTTON_R1 to "RB (Right Bumper)",
        KeyEvent.KEYCODE_BUTTON_L2 to "LT (Left Trigger)",
        KeyEvent.KEYCODE_BUTTON_R2 to "RT (Right Trigger)",
        KeyEvent.KEYCODE_BUTTON_THUMBL to "L3 (Left Stick Click)",
        KeyEvent.KEYCODE_BUTTON_THUMBR to "R3 (Right Stick Click)",
        KeyEvent.KEYCODE_BUTTON_START to "Start",
        KeyEvent.KEYCODE_BUTTON_SELECT to "Select / Back",
        KeyEvent.KEYCODE_BUTTON_MODE to "Guide / Home",
        KeyEvent.KEYCODE_DPAD_UP to "D-Pad Up",
        KeyEvent.KEYCODE_DPAD_DOWN to "D-Pad Down",
        KeyEvent.KEYCODE_DPAD_LEFT to "D-Pad Left",
        KeyEvent.KEYCODE_DPAD_RIGHT to "D-Pad Right",
        KeyEvent.KEYCODE_BUTTON_C to "C Button",
        KeyEvent.KEYCODE_BUTTON_Z to "Z Button",
    )

    val axisNames: Map<Int, String> = mapOf(
        MotionEvent.AXIS_X to "Left Stick X",
        MotionEvent.AXIS_Y to "Left Stick Y",
        MotionEvent.AXIS_Z to "Right Stick X",
        MotionEvent.AXIS_RZ to "Right Stick Y",
        MotionEvent.AXIS_LTRIGGER to "Left Trigger",
        MotionEvent.AXIS_RTRIGGER to "Right Trigger",
        MotionEvent.AXIS_HAT_X to "D-Pad X (Hat)",
        MotionEvent.AXIS_HAT_Y to "D-Pad Y (Hat)",
        MotionEvent.AXIS_BRAKE to "Brake",
        MotionEvent.AXIS_GAS to "Gas",
    )

    val keyboardKeyNames: Map<Int, String> = buildMap {
        // Letters
        ('A'..'Z').forEach { c ->
            put(KeyEvent.keyCodeFromString("KEYCODE_$c"), c.toString())
        }
        // Numbers
        (0..9).forEach { n ->
            put(KeyEvent.keyCodeFromString("KEYCODE_$n"), n.toString())
        }
        // Function keys
        (1..12).forEach { n -> put(KeyEvent.keyCodeFromString("KEYCODE_F$n"), "F$n") }
        // Special keys
        putAll(mapOf(
            KeyEvent.KEYCODE_SPACE to "Space",
            KeyEvent.KEYCODE_ENTER to "Enter",
            KeyEvent.KEYCODE_ESCAPE to "Escape",
            KeyEvent.KEYCODE_TAB to "Tab",
            KeyEvent.KEYCODE_DEL to "Backspace",
            KeyEvent.KEYCODE_FORWARD_DEL to "Delete",
            KeyEvent.KEYCODE_HOME to "Home",
            KeyEvent.KEYCODE_MOVE_END to "End",
            KeyEvent.KEYCODE_PAGE_UP to "Page Up",
            KeyEvent.KEYCODE_PAGE_DOWN to "Page Down",
            KeyEvent.KEYCODE_INSERT to "Insert",
            KeyEvent.KEYCODE_DPAD_UP to "Arrow Up",
            KeyEvent.KEYCODE_DPAD_DOWN to "Arrow Down",
            KeyEvent.KEYCODE_DPAD_LEFT to "Arrow Left",
            KeyEvent.KEYCODE_DPAD_RIGHT to "Arrow Right",
            // Modifiers
            KeyEvent.KEYCODE_CTRL_LEFT to "Ctrl",
            KeyEvent.KEYCODE_SHIFT_LEFT to "Shift",
            KeyEvent.KEYCODE_ALT_LEFT to "Alt",
            KeyEvent.KEYCODE_META_LEFT to "Win / Super",
            // Symbols
            KeyEvent.KEYCODE_COMMA to ",",
            KeyEvent.KEYCODE_PERIOD to ".",
            KeyEvent.KEYCODE_SLASH to "/",
            KeyEvent.KEYCODE_BACKSLASH to "\\",
            KeyEvent.KEYCODE_SEMICOLON to ";",
            KeyEvent.KEYCODE_APOSTROPHE to "'",
            KeyEvent.KEYCODE_LEFT_BRACKET to "[",
            KeyEvent.KEYCODE_RIGHT_BRACKET to "]",
            KeyEvent.KEYCODE_GRAVE to "`",
            KeyEvent.KEYCODE_MINUS to "-",
            KeyEvent.KEYCODE_EQUALS to "=",
            // Numpad
            KeyEvent.KEYCODE_NUMPAD_0 to "Num 0",
            KeyEvent.KEYCODE_NUMPAD_1 to "Num 1",
            KeyEvent.KEYCODE_NUMPAD_2 to "Num 2",
            KeyEvent.KEYCODE_NUMPAD_3 to "Num 3",
            KeyEvent.KEYCODE_NUMPAD_4 to "Num 4",
            KeyEvent.KEYCODE_NUMPAD_5 to "Num 5",
            KeyEvent.KEYCODE_NUMPAD_6 to "Num 6",
            KeyEvent.KEYCODE_NUMPAD_7 to "Num 7",
            KeyEvent.KEYCODE_NUMPAD_8 to "Num 8",
            KeyEvent.KEYCODE_NUMPAD_9 to "Num 9",
            KeyEvent.KEYCODE_NUMPAD_ADD to "Num +",
            KeyEvent.KEYCODE_NUMPAD_SUBTRACT to "Num -",
            KeyEvent.KEYCODE_NUMPAD_MULTIPLY to "Num *",
            KeyEvent.KEYCODE_NUMPAD_DIVIDE to "Num /",
            KeyEvent.KEYCODE_NUMPAD_ENTER to "Num Enter",
            KeyEvent.KEYCODE_NUMPAD_DOT to "Num .",
            // Media
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE to "Play/Pause",
            KeyEvent.KEYCODE_MEDIA_NEXT to "Next Track",
            KeyEvent.KEYCODE_MEDIA_PREVIOUS to "Prev Track",
            KeyEvent.KEYCODE_VOLUME_UP to "Volume Up",
            KeyEvent.KEYCODE_VOLUME_DOWN to "Volume Down",
            KeyEvent.KEYCODE_VOLUME_MUTE to "Mute",
        ))
    }

    val mouseActionNames: Map<String, String> = mapOf(
        "MOUSE_LEFT" to "Left Click",
        "MOUSE_RIGHT" to "Right Click",
        "MOUSE_MIDDLE" to "Middle Click",
        "MOUSE_MOVE_X" to "Mouse Move X-axis",
        "MOUSE_MOVE_Y" to "Mouse Move Y-axis",
    )

    fun getSourceName(sourceCode: Int, sourceType: any.mapper.data.model.SourceType): String {
        return when (sourceType) {
            any.mapper.data.model.SourceType.BUTTON ->
                controllerButtonNames[sourceCode] ?: keyboardKeyNames[sourceCode] ?: "Key $sourceCode"
            any.mapper.data.model.SourceType.AXIS_POS ->
                "${axisNames[sourceCode] ?: "Axis $sourceCode"} +"
            any.mapper.data.model.SourceType.AXIS_NEG ->
                "${axisNames[sourceCode] ?: "Axis $sourceCode"} -"
            any.mapper.data.model.SourceType.AXIS_FULL ->
                axisNames[sourceCode] ?: "Axis $sourceCode"
        }
    }

    fun getTargetName(mapping: any.mapper.data.model.Mapping): String {
        return when (mapping.targetType) {
            any.mapper.data.model.TargetType.KEY -> keyboardKeyNames[mapping.targetKeyCode] ?: "Key ${mapping.targetKeyCode}"
            any.mapper.data.model.TargetType.KEY_COMBO -> {
                val modStr = buildString {
                    if (mapping.targetModifiers and android.view.KeyEvent.META_CTRL_ON != 0) append("Ctrl+")
                    if (mapping.targetModifiers and android.view.KeyEvent.META_SHIFT_ON != 0) append("Shift+")
                    if (mapping.targetModifiers and android.view.KeyEvent.META_ALT_ON != 0) append("Alt+")
                }
                "$modStr${keyboardKeyNames[mapping.targetKeyCode] ?: "Key ${mapping.targetKeyCode}"}"
            }
            any.mapper.data.model.TargetType.MOUSE_LEFT -> "Left Click"
            any.mapper.data.model.TargetType.MOUSE_RIGHT -> "Right Click"
            any.mapper.data.model.TargetType.MOUSE_MIDDLE -> "Middle Click"
            any.mapper.data.model.TargetType.MOUSE_MOVE_X -> "Mouse X"
            any.mapper.data.model.TargetType.MOUSE_MOVE_Y -> "Mouse Y"
            any.mapper.data.model.TargetType.TEXT -> "Text"
        }
    }

    fun isControllerButton(keyCode: Int): Boolean = keyCode in controllerButtonNames

    val keyboardKeysGrouped: Map<String, List<Pair<Int, String>>> = mapOf(
        "Letters" to ('A'..'Z').map { KeyEvent.keyCodeFromString("KEYCODE_$it") to it.toString() },
        "Numbers" to (0..9).map { KeyEvent.keyCodeFromString("KEYCODE_$it") to it.toString() },
        "Function" to (1..12).map { KeyEvent.keyCodeFromString("KEYCODE_F$it") to "F$it" },
        "Navigation" to listOf(
            KeyEvent.KEYCODE_DPAD_UP to "↑", KeyEvent.KEYCODE_DPAD_DOWN to "↓",
            KeyEvent.KEYCODE_DPAD_LEFT to "←", KeyEvent.KEYCODE_DPAD_RIGHT to "→",
            KeyEvent.KEYCODE_HOME to "Home", KeyEvent.KEYCODE_MOVE_END to "End",
            KeyEvent.KEYCODE_PAGE_UP to "PgUp", KeyEvent.KEYCODE_PAGE_DOWN to "PgDn",
        ),
        "Special" to listOf(
            KeyEvent.KEYCODE_SPACE to "Space", KeyEvent.KEYCODE_ENTER to "Enter",
            KeyEvent.KEYCODE_ESCAPE to "Esc", KeyEvent.KEYCODE_TAB to "Tab",
            KeyEvent.KEYCODE_DEL to "Bksp", KeyEvent.KEYCODE_FORWARD_DEL to "Del",
            KeyEvent.KEYCODE_INSERT to "Ins",
        ),
        "Modifiers" to listOf(
            KeyEvent.KEYCODE_CTRL_LEFT to "Ctrl", KeyEvent.KEYCODE_SHIFT_LEFT to "Shift",
            KeyEvent.KEYCODE_ALT_LEFT to "Alt", KeyEvent.KEYCODE_META_LEFT to "Win",
        ),
        "Numpad" to (0..9).map { KeyEvent.keyCodeFromString("KEYCODE_NUMPAD_$it") to "N$it" } + listOf(
            KeyEvent.KEYCODE_NUMPAD_ADD to "N+", KeyEvent.KEYCODE_NUMPAD_SUBTRACT to "N-",
            KeyEvent.KEYCODE_NUMPAD_MULTIPLY to "N*", KeyEvent.KEYCODE_NUMPAD_DIVIDE to "N/",
            KeyEvent.KEYCODE_NUMPAD_ENTER to "NEnter",
        ),
    )
}
