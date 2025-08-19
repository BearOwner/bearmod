plugins {
    id 'com.android.library'
}

android {
    namespace = 'com.bearmod.mundo'
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++17 -fvisibility=hidden -ffunction-sections -fdata-sections"
                arguments "-DMUNDO_LIBRARY_BUILD=1", "-DNATIVE_RUNTIME_CONTAINER=1", "-DANDROID_ARM64_V8A=1"
            }
        }
        ndk {
            abiFilters 'arm64-v8a'
        }
    }

    buildTypes {
        release {
            minifyEnabled = false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            externalNativeBuild {
                cmake {
                    cppFlags "-O2 -DNDEBUG -DRELEASE_BUILD"
                    arguments "-DCMAKE_BUILD_TYPE=Release"
                }
            }
        }
        debug {
            jniDebuggable = true
            externalNativeBuild {
                cmake {
                    cppFlags "-g -O0 -DDEBUG_BUILD"
                    arguments "-DCMAKE_BUILD_TYPE=Debug"
                }
            }
        }
    }

    externalNativeBuild {
        cmake {
            path "src/main/cpp/CMakeLists.txt"
            version = "3.22.1"
        }
    }


    buildFeatures { prefab = false }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    ndkVersion = "27.1.12297006"
}

dependencies {
    // No external libs required for core API

}

