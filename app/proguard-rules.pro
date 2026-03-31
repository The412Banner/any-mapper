-keep class any.mapper.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}
# Keep InputManager reflection target
-keep class android.hardware.input.InputManager { *; }
-keep class android.view.InputEvent { *; }
-keep class android.view.KeyEvent { *; }
-keep class android.view.MotionEvent { *; }
