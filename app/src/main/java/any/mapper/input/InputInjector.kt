package any.mapper.input

import android.content.Context
import android.hardware.input.InputManager
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.MotionEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.reflect.Method
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InputInjector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val inputManager: InputManager =
        context.getSystemService(Context.INPUT_SERVICE) as InputManager
    private val injectMethod: Method? = runCatching {
        InputManager::class.java.getMethod("injectInputEvent", android.view.InputEvent::class.java, Int::class.java)
            .also { it.isAccessible = true }
    }.getOrNull()

    private fun inject(event: android.view.InputEvent): Boolean {
        return runCatching { injectMethod?.invoke(inputManager, event, 0) as? Boolean ?: false }
            .getOrDefault(false)
    }

    fun injectKey(keyCode: Int, action: Int, modifiers: Int = 0) {
        val now = SystemClock.uptimeMillis()
        val event = KeyEvent(
            now, now, action, keyCode, 0, modifiers,
            KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
            KeyEvent.FLAG_FROM_SYSTEM or KeyEvent.FLAG_SOFT_KEYBOARD,
            InputDevice.SOURCE_KEYBOARD
        )
        inject(event)
    }

    fun injectKeyDown(keyCode: Int, modifiers: Int = 0) = injectKey(keyCode, KeyEvent.ACTION_DOWN, modifiers)
    fun injectKeyUp(keyCode: Int, modifiers: Int = 0) = injectKey(keyCode, KeyEvent.ACTION_UP, modifiers)

    fun injectKeyCombo(modifierKeyCodes: List<Int>, keyCode: Int) {
        modifierKeyCodes.forEach { injectKeyDown(it) }
        injectKeyDown(keyCode)
        injectKeyUp(keyCode)
        modifierKeyCodes.reversed().forEach { injectKeyUp(it) }
    }

    fun injectMouseMove(x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_HOVER_MOVE, x, y, 0).also {
            it.source = InputDevice.SOURCE_MOUSE
        }
        inject(event)
        event.recycle()
    }

    fun injectMouseButtonDown(button: Int = MotionEvent.BUTTON_PRIMARY) {
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_BUTTON_PRESS, 0f, 0f, 0).also {
            it.source = InputDevice.SOURCE_MOUSE
        }
        inject(event)
        event.recycle()
    }

    fun injectMouseButtonUp(button: Int = MotionEvent.BUTTON_PRIMARY) {
        val now = SystemClock.uptimeMillis()
        val event = MotionEvent.obtain(now, now, MotionEvent.ACTION_BUTTON_RELEASE, 0f, 0f, 0).also {
            it.source = InputDevice.SOURCE_MOUSE
        }
        inject(event)
        event.recycle()
    }

    val isAvailable: Boolean get() = injectMethod != null
}
