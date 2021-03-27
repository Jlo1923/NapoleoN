package com.naposystems.napoleonchat.model

import com.naposystems.napoleonchat.utility.Constants
import java.io.Serializable

data class CallModel(
    var contactId: Int = 0,
    var channelName: String = "",
    var isVideoCall: Boolean = false,
    val offer: String = "",
    var typeCall: Constants.TypeCall = Constants.TypeCall.IS_OUTGOING_CALL
) : Serializable