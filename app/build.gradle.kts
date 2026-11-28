plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// کلید debug ثابت (محرمانه نیست و فقط برای debug است): بدون آن، هر ساخت در CI کلید تازه‌ای می‌سازد
// و اثر انگشت SHA1 عوض می‌شود (مجوز نقشه به آن وابسته است). هرگز برای release استفاده نشود.
val debugKeystoreB64 = file("debug-keystore.b64")
val debugKeystore = layout.buildDirectory.file("debug.keystore").get().asFile
if (debugKeystoreB64.exists()) {
    debugKeystore.parentFile.mkdirs()
    debugKeystore.writeBytes(java.util.Base64.getMimeDecoder().decode(debugKeystoreB64.readText()))
}

android {
    namespace = "ir.dorehami.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ir.dorehami.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        getByName("debug") {
            if (debugKeystore.exists()) {
                storeFile = debugKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug { applicationIdSuffix = ".debug" }
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    sourceSets["main"].java.srcDirs("src/main/kotlin")
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    testImplementation("junit:junit:4.13.2")
}
