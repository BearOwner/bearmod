import java.io.File
import java.security.SecureRandom
// (no java.time imports needed after removing output renaming tasks)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}


// Local signing can be configured below in Kotlin DSL. External Groovy apply removed to avoid conflicts.

// Toggle native build with -PenableNativeBuild=true (defaults to false)
val enableNativeBuild: Boolean =
    (project.findProperty("enableNativeBuild") as String?)?.toBoolean() ?: false

// --- Thread-safe scrambler for randomized app_name ---
object Scrambler {
    private val secureRandom = SecureRandom()
    fun getRandomString(length: Int): String {
        val len = 6 + secureRandom.nextInt(length)
        val chars = ('a'..'z').toList()
        return buildString {
            repeat(len) {
                append(chars[secureRandom.nextInt(chars.size)])
            }
        }
    }
}

val randomLabel by lazy { Scrambler.getRandomString(10).uppercase() }

android {
    namespace = "com.bearmod"
    compileSdk = 36
    lint {
        baseline = file("lint-baseline.xml")
    }
    defaultConfig {
        applicationId = "com.bearmod"
        minSdk = 28
        targetSdk = 35
        versionCode = 100
        versionName = "3.0.0"
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters.add("arm64-v8a") // This tells Gradle to only build for arm64-v8a
        }
        multiDexEnabled = false

            // Development app configuration
        buildConfigField("String", "MUNDO_VERSION", "\"1.0.0\"")
        buildConfigField("String", "APP_TYPE", "\"DEVELOPMENT\"")
        buildConfigField("boolean", "DEBUG_TOOLS_ENABLED", "true")

        // BearMod v3.0 Configuration
        buildConfigField("boolean", "NONROOT_INJECTION_ENABLED", "true")
        buildConfigField("boolean", "ENHANCED_SECURITY_ENABLED", "true")
        buildConfigField("String", "BEARMOD_VERSION", "\"3.0.0\"")

    }


    // APK Signing Configuration (supports local dev and CI via env vars)
    signingConfigs {
        create("release") {
            // Priority for keystore path:
            // 1) RELEASE_KEYSTORE_PATH (decoded in CI from RELEASE_KEYSTORE_B64)
            // 2) ANDROID_KEYSTORE (provided by CI workflow)
            // 3) BEARMOD_KEYSTORE_PATH or Gradle property
            // 4) project root: release.keystore (if created locally)
            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
                ?: System.getenv("ANDROID_KEYSTORE")
                ?: System.getenv("BEARMOD_KEYSTORE_PATH")
                ?: (project.findProperty("BEARMOD_KEYSTORE_PATH") as String?)
                ?: file("release.keystore").absolutePath

            val ks = file(keystorePath)
            if (ks.exists()) {
                storeFile = ks
                // Password sources (priority): RELEASE_* -> ANDROID_* -> BEARMOD_* -> Gradle props
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                    ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
                    ?: System.getenv("BEARMOD_KEYSTORE_PASSWORD")
                    ?: project.findProperty("RELEASE_KEYSTORE_PASSWORD") as String?
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                    ?: System.getenv("ANDROID_KEY_ALIAS")
                    ?: System.getenv("BEARMOD_KEY_ALIAS")
                    ?: project.findProperty("RELEASE_KEY_ALIAS") as String?
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
                    ?: System.getenv("ANDROID_KEY_ALIAS_PASSWORD")
                    ?: System.getenv("BEARMOD_KEY_PASSWORD")
                    ?: project.findProperty("RELEASE_KEY_PASSWORD") as String?

                // Enable signature schemes
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true

                println("✅ Release signing configured with keystore: ${ks.absolutePath}")
            } else {
                println("⚠️  Keystore not found at: ${ks.absolutePath}")
                println("   Release builds will be unsigned unless CI injects signing via -Pandroid.injected.signing.* or env.")
            }
        }

        // Debug signing config (uses default debug keystore)
        getByName("debug") {
            // Android Studio default debug keystore
            // This will be automatically used for debug builds
        }
    }

/* (kept intentionally minimal; old Groovy-style examples removed) */
        buildTypes {
        debug {
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
            // Use debug signing config (default Android debug keystore)
            signingConfig = signingConfigs.getByName("debug")
            // Development/debug configuration
            buildConfigField("boolean", "MUNDO_INTEGRATION", "true")
            buildConfigField("String", "BUILD_TYPE", "\"DEBUG\"")
            buildConfigField(
                "String[]",
                "SUPPORTED_GAMES",
                "{\"com.tencent.ig\", \"com.pubg.krmobile\", \"com.reko.pubgm\", \"com.vng.pubgmobile\", \"com.battlegroundmobile.india\"}"
            )
        }
        release {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-obfuscation.pro"
            )
            // Use release signing config (CI/local env-driven)
            signingConfig = signingConfigs.getByName("release")
            // Release configuration for testing
            buildConfigField("boolean", "MUNDO_INTEGRATION", "true")//latter
            buildConfigField("String", "BUILD_TYPE", "\"RELEASE\"")
            buildConfigField(
                "String[]",
                "SUPPORTED_GAMES",
                "{\"com.tencent.ig\", \"com.pubg.krmobile\", \"com.reko.pubgm\", \"com.vng.pubgmobile\", \"com.battlegroundmobile.india\"}"
            )

            // BearMod v3.0 Release Configuration
            buildConfigField("boolean", "NONROOT_INJECTION_ENABLED", "true")
            buildConfigField("boolean", "ENHANCED_SECURITY_ENABLED", "true")
            buildConfigField("String", "BEARMOD_VERSION", "\"3.0.0\"")
        
        }
    }

    packaging {
        jniLibs {
            pickFirsts.addAll(
                listOf(
                    "**/libclient_static.so", //anti_hook (integrated into libbearmod.so)
                    "**/libc++_shared.so"
                  //  "**/libmundo.so" //mundo_core
                )
            )
        }
    }

    if (enableNativeBuild) {
        externalNativeBuild {
            ndkBuild {
                path = file("src/main/cpp/Android.mk")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        viewBinding = true  // Enable ViewBinding
        buildConfig = true  // Enable BuildConfig generation
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    ndkVersion = "27.1.12297006"
}

// Note: Removed androidComponents output renaming and mapping archival tasks to maintain AGP 8.2 compatibility.

dependencies {
    implementation(fileTree("libs") { include("*.jar") })

    //implementation(project(":mundo_core"))

    implementation(libs.androidx.multidex)

    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.viewpager)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.annotation)
    implementation("androidx.documentfile:documentfile:1.0.1")

    implementation(libs.androidx.dynamicanimation.ktx)
    implementation(libs.androidx.interpolator)

    // Image loading
  //  implementation(libs.glide)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.gson)

    implementation(libs.androidx.security.crypto)
    implementation(libs.commons.io)
    implementation(libs.timber)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.okhttp.mockwebserver)
}
