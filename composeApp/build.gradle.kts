import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqlDelight)
}

kotlin {
    androidLibrary {
        namespace = "ir.salamat.composeApp"
        compileSdk = 37
        minSdk = 24
        withJava()
        withHostTest {}
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    js {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.composeRuntime)
            implementation(libs.composeFoundation)
            implementation(libs.composeMaterial3)
            implementation(libs.composeResources)
            implementation(libs.composeUiToolingPreview)
            implementation(libs.materialIconsCore)

            implementation(libs.jetbrainsNavigationCompose)
            implementation(libs.jetbrainViewModel)
            implementation(libs.jetbrainsLifecycleRuntimeCompose)

            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeVM)

            implementation(libs.kotlinxDatetime)
            implementation(libs.kotlinxSerialization)
            implementation(libs.kotlinSerializationJson)
            implementation(libs.kotlinxCoroutinesCore)

            implementation(libs.sql.delight.coroutines.extensions)
        }

        androidMain.dependencies {
            implementation(libs.composeUiTooling)
            implementation(libs.koinAndroid)
            implementation(libs.android.driver)
        }

        iosMain.dependencies {
            implementation(libs.sql.delight.native.driver)
        }

        jsMain.dependencies {
            implementation(libs.web.worker.driver)
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.0.2"))
            implementation(npm("sql.js", "^1.8.0"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinCoroutineTest)
            implementation(libs.turbineTest)
        }
    }
}

sqldelight {
    databases {
        create("SalamatDatabase") {
            packageName.set("ir.salamat.database")
            generateAsync.set(true)
        }
    }
}
