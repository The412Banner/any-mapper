package any.mapper.service

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import any.mapper.input.InputInjector
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MouseCursorManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val injector: InputInjector
) {
    private val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var cursorX = 0f
    private var cursorY = 0f
    private var deltaX = 0f
    private var deltaY = 0f
    private var scope: CoroutineScope? = null

    private val screenWidth: Float get() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels.toFloat()
    }

    private val screenHeight: Float get() {
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels.toFloat()
    }

    fun start() {
        if (scope != null) return
        cursorX = screenWidth / 2f
        cursorY = screenHeight / 2f
        scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope?.launch {
            while (isActive) {
                if (deltaX != 0f || deltaY != 0f) {
                    cursorX = (cursorX + deltaX).coerceIn(0f, screenWidth)
                    cursorY = (cursorY + deltaY).coerceIn(0f, screenHeight)
                    injector.injectMouseMove(cursorX, cursorY)
                }
                delay(16L) // ~60fps
            }
        }
    }

    fun stop() {
        scope?.cancel()
        scope = null
    }

    fun updateAxis(axisX: Float, axisY: Float, sensitivity: Float) {
        deltaX = axisX * sensitivity * SPEED_MULTIPLIER
        deltaY = axisY * sensitivity * SPEED_MULTIPLIER
    }

    fun resetCursor() {
        cursorX = screenWidth / 2f
        cursorY = screenHeight / 2f
        injector.injectMouseMove(cursorX, cursorY)
    }

    companion object {
        private const val SPEED_MULTIPLIER = 8f
    }
}
