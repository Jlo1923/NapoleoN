package com.naposystems.napoleonchat.ui.custom.microphoneRecorderView

interface IContractMicrophoneRecorder {
    fun setListener(listener: MicrophoneRecorderView.Listener)
    fun cancelAction()
    fun isRecordingLocked(): Boolean
    fun unlockAction()
}