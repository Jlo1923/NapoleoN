package com.naposystems.pepito.ui.custom.fabSend

import com.naposystems.pepito.databinding.CustomViewFabSendBinding

interface IContractFabSend {

    fun setListener(listener: FabSend.FabSendListener)

    fun morphToSend()

    fun morphToMic()

    fun isShowingMic(): Boolean

    fun setContainerLock(constraintLayout: CustomViewFabSendBinding)

    fun isLocked(): Boolean

    fun reset()
}