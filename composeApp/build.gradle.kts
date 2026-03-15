import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias (libs.plugins.googleServices)

    // ✅ REQUIRED for cocoapods {}
    kotlin("native.cocoapods")

    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    // -----------------------------
    // ANDROID TARGET (MANDATORY)
    // -----------------------------
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    // -----------------------------
    // IOS + COCOAPODS CONFIG
    // -----------------------------
    cocoapods {
        // REQUIRED podspec version
        version = "1.0.0"

        ios.deploymentTarget = "15.6"
        summary = "ComposeApp shared KMP framework"
        homepage = "https://example.com"

        // Native iOS WebRTC SDK (MANDATORY for webrtc-kmp)
        pod("WebRTC-SDK") {
            version = "125.6422.05"   // Must match WebRTC M125
            moduleName = "WebRTC"
        }

        framework {
            baseName = "ComposeApp"
            isStatic = true
        }

        // Podfile location
        podfile = project.file("../iosApp/Podfile")
    }

    // iOS targets
    iosArm64()
    iosSimulatorArm64()

    // -----------------------------
    // SOURCE SETS
    // -----------------------------
    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.trixnity.client.media.okio)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.bundles.camerax)
            implementation(libs.bundles.media3)
            implementation(libs.firebase.messaging)
          //  implementation("net.folivo:trixnity-client-repository-realm:4.11.2")
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
           // implementation(libs.material3.expressive)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.co.kermit.logger)
            implementation(libs.coil.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.coil.network.ktor)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.resources)

            implementation(libs.trixnity.client)
            implementation(libs.trixnity.client.repository.room)

            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)

            implementation(libs.multiplatform.settings)
            implementation(libs.kotlinInject.runtime.kmp)
            implementation(libs.bundles.kotlinInjectAnvil)
            implementation(libs.kotlinx.serialization.json)


            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.driver.bundled)

            implementation(libs.icons.fontawesome)
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)

            // WebRTC KMP
            implementation(libs.webrtc.kmp)

            implementation(libs.bundles.arrow)

            // Project modules
            implementation(projects.libraries.gsnCore)
            implementation(projects.libraries.gsnTheme)
            implementation(projects.libraries.sharedRes)
            implementation(projects.libraries.designcomponents)
            implementation(projects.libraries.kmputils)
            implementation(projects.libraries.di)
            implementation(projects.appDi)
            implementation(projects.libraries.gsnMatrix.impl)
            implementation(projects.appconfig)
            implementation(projects.libraries.architecture)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.trixnity.client.media.okio)
          //  implementation("net.folivo:trixnity-client-repository-realm:4.11.2")
        }
    }
}

// -----------------------------
// ANDROID CONFIG
// -----------------------------
android {
    namespace = "com.example.getsafenowclient"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.getsafenowclient"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// -----------------------------
// DEBUG / KSP / ROOM
// -----------------------------
dependencies {
    debugImplementation(compose.uiTooling)
}

ksp {
    // args if you need them later, e.g.:
    // arg("me.tatarka.inject.generateCompanionExtensions", "true")
}

compose.resources {
    publicResClass = true
    // Add this to merge resources from shared-res module
    generateResClass = always
    // This will make shared-res resources available in composeApp
    packageOfResClass = "com.example.getsafenowclient.generated.resources"
}

dependencies {
    // For common metadata (use the KMP-aware compiler artifact)
    add("kspCommonMainMetadata", libs.kotlinInject.compiler)

    // For each target (use the regular KSP compiler)
    add("kspAndroid", libs.kotlinInject.compiler)
//    add("kspIosX64", libs.kotlinInject.compiler)
    add("kspIosArm64", libs.kotlinInject.compiler)
    add("kspIosSimulatorArm64", libs.kotlinInject.compiler)
}

dependencies{
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
