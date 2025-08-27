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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

##############################################
# ✅ Obfuscation dictionaries (stealth/randomized names)
# These files live under the app module at app/obf/
##############################################
-obfuscationdictionary obf/class_dict.txt
-classobfuscationdictionary obf/class_dict.txt
-packageobfuscationdictionary obf/package_dict.txt

##############################################
# ✅ Reproducible mapping controls
# -printmapping: persist obfuscation map for crash deobfuscation and reuse
# -applymapping: keep prior obfuscated names stable across patch/minor builds
##############################################
-printmapping obf/mapping-current.txt
# For patch/minor builds based on previous release, uncomment and point to the
# archived mapping of that release (do NOT ship this file in artifacts):
# -applymapping obf/mapping-<version>.txt

##############################################
# ✅ Keep minimal classes needed for startup/bootstrap
##############################################
# Keep BearMod core auth and JNI bridge classes where reflection/JNI is used
-keep class com.bearmod.activity.LoginActivity { *; }
-keep class com.bearmod.Floating { *; }
-keep class com.bearmod.bridge.NativeLib { *; }
-keep class com.bearmod.activity.MainActivity { *; }
-keep class com.bearmod.activity.SplashActivity { *; }

# Keep lightweight string obfuscator and logging wrapper
-keep class com.bearmod.util.StrObf { *; }
-keep class com.bearmod.util.Logx { *; }

# Preserve OkHttp and retrofit models (if any)
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Stealth: do NOT keep auth/security/injection packages so R8 can obfuscate/rename/elide them
# (If any of these classes must expose native methods at runtime, we will pass Class objects from Java
#  and register by jclass to avoid FindClass on string names.)

##############################################
# ✅ Keep classes referenced in JNI method signatures
##############################################
-keep class com.bearmod.ESPView { *; }

##############################################
# ✅ Keep all native methods (do not obfuscate their names/signatures)
##############################################
-keepclasseswithmembernames class * {
    native <methods>;
}

##############################################
# ✅ Preserve attributes needed for reflection/JNI
##############################################
-keepattributes Signature,InnerClasses,EnclosingMethod

##############################################
# ✅ Keep method signatures referenced from C++
# Example: RegisterNatives -> "iconenc" "()Ljava/lang/String;"
##############################################
#-keepclassmembers class com.bearmod.Floating {
  #  public native java.lang.String iconenc();
   # public native boolean IsHideEsp();
#}

##############################################
# ✅ Optional: if you call JNI via reflection
##############################################
# -keep class com.bearmod.** { *; }
