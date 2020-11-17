package com.naposystems.napoleonchat.ui.custom.circleProgressBar

interface IContractCircleProgressBar {
    fun getProgress(): Float
    fun setProgressColor(colorId: Int)
    fun setProgress(progress: Float)
}