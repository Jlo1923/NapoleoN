package com.naposystems.napoleonchat.ui.custom.fabSend

import com.naposystems.napoleonchat.databinding.CustomViewFabSendBinding

interface IContractFabSend {

    fun setListener(listener: FabSend.FabSendListener)

    fun morphToSend()

    fun morphToMic()

    fun isShowingMic(): Boolean

    fun setContainerLock(constraintLayout: CustomViewFabSendBinding)

    fun isLocked(): Boolean

    fun reset()
}