# Keep ZXing's CaptureActivity entry point and intent extras.
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }

# Compose / Kotlin stdlib reflection.
-dontwarn org.jetbrains.annotations.**
