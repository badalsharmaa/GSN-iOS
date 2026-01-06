package com.example.getsafenowclient.audio

import io.getsafenow.libraries.kmputils.platformkmp.PlatformFile

expect class AudioRecorderImpl() : AudioRecorder {
    override fun start(outputFile: PlatformFile)
    override fun stop()
    override fun cancel()
    override fun isRecording(): Boolean
    override fun getMaxAmplitude(): Float
}
