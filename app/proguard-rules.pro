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
# ✅ Keep classes directly used in JNI (FindClass)
##############################################
-keep class com.bearmod.Floating { *; }
-keep class com.bearmod.activity.LoginActivity { *; }
-keep class com.bearmod.activity.MainActivity { *; }
-keep class com.bearmod.activity.SplashActivity { *; }

# Keep auth classes referenced via reflection/JNI
-keep class com.bearmod.auth.SimpleLicenseVerifier { *; }
# If other auth helpers exist, keep the whole package (safe option)
-keep class com.bearmod.auth.** { *; }

# Keep plugin/manager classes referenced by native RegisterNatives/FindClass
-keep class com.bearmod.plugin.NonRootManager { *; }
-keep class com.bearmod.patch.NonRootPatchManager { *; }
-keep class com.bearmod.loader.security.NativeSecurityManager { *; }

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
