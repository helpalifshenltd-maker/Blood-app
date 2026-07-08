# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep all data models and classes in com.example.data
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Keep all UI State classes and Jetpack Compose/Serializer models
-keep class com.example.ui.** { *; }
-keepclassmembers class com.example.ui.** { *; }

# Keep Moshi-generated JSON adapters
-keep class *JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }

# Keep Retrofit classes
-keep class retrofit2.** { *; }

# Keep Room classes and annotations
-keep class androidx.room.** { *; }

# OkHttp/Retrofit ProGuard Rules to bypass optional dependency warnings
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn retrofit2.Platform$Java8
-dontwarn retrofit2.Platform$Java9
-dontwarn retrofit2.Platform$Java14

# Keep Kotlinx Serialization or general reflective utilities
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
