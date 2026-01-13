plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.buildkonfig)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.getsafenow.appconfig"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "appconfigKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.components.resources)
                // Add KMP dependencies here
                //-------------------------------------------------------------//
                implementation(projects.libraries.kmputils)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}
buildkonfig {
    packageName = "io.getsafenow.appconfig"

    // Optionally read from gradle.properties or CI env (with sane defaults)
    // plugins ones-

    val isEnterprise = providers.gradleProperty("APP_IS_ENTERPRISE_BUILD")
        .map { it.toBoolean() }
        .orElse(false)
    val defaultHomeserver = providers.gradleProperty("APP_DEFAULT_HOMESERVER")
        .orElse("https://spydefense.org")
    val slidingSyncProxy = providers.gradleProperty("APP_SLIDING_SYNC_PROXY")
        .orElse("")
    val oidcEnabled = providers.gradleProperty("APP_OIDC_ENABLED")
        .map { it.toBoolean() }
        .orElse(false)
    val analyticsEnabled = providers.gradleProperty("APP_ANALYTICS_ENABLED")
        .map { it.toBoolean() }
        .orElse(true)
    val clientAppName = providers.gradleProperty("CLIENT_APP_NAME")
        .orElse("GetSafeNowClient")
    val appName = providers.gradleProperty("APP_NAME")
        .orElse("GetSafeNow")
    val appVersion = providers.gradleProperty("APP_VERSION")
        .orElse("1.0")
    val deviceNameFormat = providers.gradleProperty("APP_DEVICE_NAME_FORMAT")
        .orElse("GetSafeNow %s") // %s replaced by platform label, e.g. Android/iOS
    // appconfig ones--
    val urlPolicy = providers.gradleProperty("APP_URL_POLICY")
        .orElse("https://getsafenow.app/cookies_policy.html")
    val bugReportUrl = providers.gradleProperty("APP_BUG_REPORT_URL")
        .orElse("https://getsafenow.im/bugreports/submit")
    val bugReportAppName = providers.gradleProperty("APP_BUG_REPORT_APP_NAME")
        .orElse("getsafenow")

    val posthogHost = providers.gradleProperty("APP_POSTHOG_HOST").orElse("")
    val posthogApiKey = providers.gradleProperty("APP_POSTHOG_APIKEY").orElse("")
    val sentryDsn = providers.gradleProperty("APP_SENTRY_DSN").orElse("")

    val pushIncludeFirebase = providers.gradleProperty("APP_PUSH_INCLUDE_FIREBASE")
        .map { it.toBoolean() }.orElse(true)
    val pushIncludeUnifiedPush = providers.gradleProperty("APP_PUSH_INCLUDE_UNIFIED_PUSH")
        .map { it.toBoolean() }.orElse(true)


    //oidc variables-
    val clientUri = providers.gradleProperty("APP_CLIENT_URI")
        .orElse("https://getsafenow.app/")
    val logoUri = providers.gradleProperty("APP_LOGO_URI")
        .orElse("https://getsafenow.app/logo.png")
    val tosUri = providers.gradleProperty("APP_TOS_URI")
        .orElse("https://getsafenow.app/terms")
    val policyUri = providers.gradleProperty("APP_POLICY_URI")
        .orElse("https://getsafenow.app/privacy")

    ///--
    val isDebug = providers.gradleProperty("APP_DEBUG").map { it.toBoolean() }.orElse(false)
    val defaultUser = providers.gradleProperty("APP_DEFAULT_USER").orElse("")
    val defaultPwd = providers.gradleProperty("APP_DEFAULT_PWD").orElse("")

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "IS_ENTERPRISE_BUILD",
            isEnterprise.get().toString()
        )

        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "DEFAULT_HOMESERVER",
            defaultHomeserver.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SLIDING_SYNC_PROXY",
            slidingSyncProxy.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "OIDC_ENABLED",
            oidcEnabled.get().toString()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "ANALYTICS_ENABLED",
            analyticsEnabled.get().toString()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "CLIENT_APP_NAME",
            clientAppName.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "APP_NAME",
            appName.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "APP_VERSION",
            appVersion.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "DEVICE_NAME_FORMAT",
            deviceNameFormat.get()
        )
        //appconfig ones-

        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "URL_POLICY",
            urlPolicy.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "BUG_REPORT_URL",
            bugReportUrl.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "BUG_REPORT_APP_NAME",
            bugReportAppName.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "POSTHOG_HOST",
            posthogHost.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "POSTHOG_APIKEY",
            posthogApiKey.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "SENTRY_DSN",
            sentryDsn.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "PUSH_INCLUDE_FIREBASE",
            pushIncludeFirebase.get().toString()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "PUSH_INCLUDE_UNIFIED_PUSH",
            pushIncludeUnifiedPush.get().toString()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "CLIENT_URI",
            clientUri.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "LOGO_URI",
            logoUri.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "TOS_URI",
            tosUri.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "POLICY_URI",
            policyUri.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN,
            "DEBUG",
            isDebug.get().toString()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "DEFAULT_USER",
            defaultUser.get()
        )
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "DEFAULT_PWD",
            defaultPwd.get()
        )
    }

    // Uncomment to override per-target if you need environment-specific values later:
    // targetConfigs {
    //     ios {
    //         buildConfigField(STRING, "DEVICE_NAME_FORMAT", "GetSafeNow iOS")
    //     }
    //     android {
    //         buildConfigField(STRING, "DEVICE_NAME_FORMAT", "GetSafeNow Android")
    //     }
    // }
}