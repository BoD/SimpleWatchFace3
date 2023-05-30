# https://github.com/androidx/androidx/tree/androidx-main/wear/watchface/watchface-complications-rendering doesn't seem to publish
# proguard rules, so do it ourselves.
-keep class androidx.wear.watchface.complications.rendering.ComplicationDrawable { *; }
