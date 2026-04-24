plugins {
    alias(libs.plugins.android.application)
}

fun String.toBuildConfigString(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

fun Project.stringPropertyOrDefault(
    name: String,
    defaultValue: String = ""
): String {
    return (findProperty(name) as String?)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: defaultValue
}

android {
    namespace = "com.example.cuutro"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.cuutro"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val backendBaseUrl = project.stringPropertyOrDefault(
            name = "backendBaseUrl",
            defaultValue = "http://10.0.2.2:8080"
        )
        val backendStaticBearerToken = project.stringPropertyOrDefault("backendBearerToken")
        val backendUsername = project.stringPropertyOrDefault("backendUsername")
        val backendPassword = project.stringPropertyOrDefault("backendPassword")

        buildConfigField(
            "String",
            "BACKEND_BASE_URL",
            "\"${backendBaseUrl.toBuildConfigString()}\""
        )
        buildConfigField(
            "String",
            "BACKEND_STATIC_BEARER_TOKEN",
            "\"${backendStaticBearerToken.toBuildConfigString()}\""
        )
        buildConfigField(
            "String",
            "BACKEND_USERNAME",
            "\"${backendUsername.toBuildConfigString()}\""
        )
        buildConfigField(
            "String",
            "BACKEND_PASSWORD",
            "\"${backendPassword.toBuildConfigString()}\""
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }

        release {
            isMinifyEnabled = false
            manifestPlaceholders["usesCleartextTraffic"] = "false"

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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.play.services.mlkit.text.recognition.common)
    implementation(libs.play.services.location)
    implementation(libs.android.sdk.opengl)
    implementation(libs.android.sdk.turf)
    implementation("io.github.track-asia:android-plugin-localization-v9:2.0.1") {
        exclude(group = "io.github.track-asia", module = "android-sdk")
    }
    implementation("io.github.track-asia:android-plugin-annotation-v9:2.0.1") {
        exclude(group = "io.github.track-asia", module = "android-sdk")
    }
    implementation("io.github.track-asia:android-plugin-markerview-v9:2.0.1") {
        exclude(group = "io.github.track-asia", module = "android-sdk")
    }
    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection)
    implementation(libs.logging.interceptor)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}