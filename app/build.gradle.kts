import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.androidGradlePlugin)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(17)
}

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
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(libs.composeMaterial3)
    implementation(libs.koinAndroid)
}
