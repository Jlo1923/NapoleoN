package com.naposystems.napoleonchat.model

import com.naposystems.napoleonchat.utility.Constants

//region Funciones de Extension de Post
fun Map<String, String>.toCallModel(): CallModel {

    var channel = ""
    var contactId = 0
    var isVideoCall = false
    var offer = ""

    if (containsKey(Constants.CallKeys.CHANNEL_NAME))
        channel = "presence-${this[Constants.CallKeys.CHANNEL_NAME]}"

    if (containsKey(Constants.CallKeys.IS_VIDEO_CALL))
        isVideoCall = this[Constants.CallKeys.IS_VIDEO_CALL] == "true"

    if (containsKey(Constants.CallKeys.CONTACT_ID))
        contactId = this[Constants.CallKeys.CONTACT_ID]?.toInt() ?: 0

    if (containsKey(Constants.CallKeys.OFFER))
        offer = this[Constants.CallKeys.OFFER].toString()

    return CallModel(
        contactId,
        channel,
        isVideoCall,
        offer
    )
}