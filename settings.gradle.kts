rootProject.name = "GetSafeNowClient"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven("https://repo.korge.org/maven")
    }
}

include(":composeApp")
include(":libraries")
include(":libraries:sessionstorage")
include(":libraries:sessionstorage:impl")
include(":libraries:sessionstorage:api")
include(":libraries:gsn_core")
include(":libraries:di")
include(":gsn-services")
include(":gsn-services:toolkit")
include(":gsn-services:toolkit:api")
include(":gsn-services:toolkit:impl")
include(":app-di")
include(":gsn-services:gsn-error")
include(":gsn-services:gsn-error:api")
include(":libraries:gsn-showcase")
include(":libraries:gsn-theme")
include(":libraries:designcomponents")
include(":libraries:shared-res")
include(":features")
include(":features:api")
include(":libraries:architecture")
include(":libraries:gsn-matrix")
include(":libraries:gsn-matrix:api")
include(":libraries:kmputils")
include(":libraries:gsn-matrix:impl")
include(":appconfig")
include(":libraries:network")
include(":libraries:featureflag")
include(":configHelper")
include(":libraries:featureflag:api")
include(":libraries:featureflag:impl")
include(":libraries:preferences")
include(":libraries:preferences:api")
include(":libraries:preferences:impl")
