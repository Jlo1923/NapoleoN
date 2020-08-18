package com.naposystems.napoleonchat.ui.custom.cameraButton

interface IContractCameraButton {
    fun setAllowSlide(allowSlide: Boolean)
    fun setListener(cameraButtonListener: CameraButton.CameraButtonListener)
    fun setMaxY(maxY: Float)
    fun isLocked(): Boolean
}