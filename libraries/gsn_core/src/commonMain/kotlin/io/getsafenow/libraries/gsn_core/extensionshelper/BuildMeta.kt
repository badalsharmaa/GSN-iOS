package io.getsafenow.libraries.gsn_core.extensionshelper

import io.element.android.libraries.core.meta.BuildMeta
import io.getsafenow.libraries.gsn_core.meta.BuildType

fun BuildMeta.isGetSafeNow(): Boolean {
    return when (buildType) {
        BuildType.RELEASE -> applicationId == "com.getsafenow.app"
        BuildType.DEBUG -> applicationId == "com.getsafenow.app.debug"
        BuildType.STAGING -> applicationId == "com.getsafenow.app.staging"
    }
}

