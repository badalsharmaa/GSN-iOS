package com.example.getsafenowclient.call

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object SharedCallManager {
    var controller: CallKitController? = null
    var onAnswerCallback: (() -> Unit)? = null
    var onHangupCallback: (() -> Unit)? = null
}
