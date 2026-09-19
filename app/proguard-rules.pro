# Add project specific ProGuard rules here.
# Release build currently ships with minification disabled (see app/build.gradle.kts),
# so these rules are not active yet. Kept in place for when release shrinking is enabled.

-keepattributes *Annotation*
-keep class androidx.media3.** { *; }
