# Keep ProGuard/R8 rules for Firebase & Compose
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-dontwarn org.bouncycastle.**
-keep class com.google.firebase.** { *; }
-keep class com.mindora.app.** { *; }
