package com.example.getsafenowclient.camera

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.StableRef
import platform.AVFoundation.AVPlayerLayer
import platform.UIKit.UIView
import platform.objc.OBJC_ASSOCIATION_RETAIN
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject


@OptIn(ExperimentalForeignApi::class)
val PlayerLayerKey: COpaquePointer = StableRef.create("playerLayerKey").asCPointer()

@OptIn(ExperimentalForeignApi::class)
fun UIView.setPlayerLayer(layer: AVPlayerLayer) {
    objc_setAssociatedObject(
        this,
        PlayerLayerKey,
        layer,
        OBJC_ASSOCIATION_RETAIN
    )
}

@OptIn(ExperimentalForeignApi::class)
fun UIView.getPlayerLayer(): AVPlayerLayer? {
    return objc_getAssociatedObject(
        this,
        PlayerLayerKey
    ) as? AVPlayerLayer
}
