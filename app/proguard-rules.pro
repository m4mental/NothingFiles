# Proguard rules for Nothing Files
-dontobfuscate
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
