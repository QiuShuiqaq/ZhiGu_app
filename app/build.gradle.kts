plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.zhiguapp"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.zhiguapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["applicationClass"] = "com.zhiguapp.ZhiGuApplication"
        manifestPlaceholders["djiApiKey"] = ""

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "runtime"
    productFlavors {
        create("demo") {
            dimension = "runtime"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            manifestPlaceholders["applicationClass"] = "com.zhiguapp.ZhiGuApplication"
            manifestPlaceholders["djiApiKey"] = ""
            buildConfigField("boolean", "DJI_ENABLED", "false")
        }
        create("dji") {
            dimension = "runtime"
            manifestPlaceholders["applicationClass"] = "com.zhiguapp.dji.ZhiGuDjiApplication"
            manifestPlaceholders["djiApiKey"] = "5e7c8f3fc9b83dc7a67a9365"
            buildConfigField("boolean", "DJI_ENABLED", "true")
            ndk {
                abiFilters += "arm64-v8a"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += setOf(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so"
            )
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.navigation.compose)
    "djiImplementation"(libs.dji.sdk.aircraft)
    "djiCompileOnly"(libs.dji.sdk.aircraft.provided)
    "djiRuntimeOnly"(libs.dji.sdk.network)
    "djiImplementation"(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
