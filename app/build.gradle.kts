import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    alias(libs.plugins.androidGradlePlugin)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(17)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val releaseKeyPath = localProperties.getProperty("release.storeFile")
    ?: System.getenv("RELEASE_STORE_FILE")
    ?: ""

val releaseStorePassword = localProperties.getProperty("release.storePassword")
    ?: System.getenv("RELEASE_STORE_PASSWORD")
    ?: ""

val releaseKeyAlias = localProperties.getProperty("release.keyAlias")
    ?: System.getenv("RELEASE_KEY_ALIAS")
    ?: ""

val releaseKeyPassword = localProperties.getProperty("release.keyPassword")
    ?: System.getenv("RELEASE_KEY_PASSWORD")
    ?: ""

extensions.configure<ApplicationExtension> {
    namespace = "ir.behpay.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "ir.behpay.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keyFile = file(releaseKeyPath)
            if (keyFile.exists() && releaseStorePassword.isNotBlank() && releaseKeyAlias.isNotBlank()) {
                storeFile = keyFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword.ifBlank { releaseStorePassword }
            }
        }
    }

    buildTypes {
        release {
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true && releaseStorePassword.isNotBlank()) {
                signingConfig = releaseSigning
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidxActivityCompose)
    implementation(libs.composeMaterial3)
    implementation(libs.koinAndroid)

    // UI tooling only in debug variant to avoid leaking PreviewActivity in release
    debugImplementation(libs.composeUiTooling)
}
