package com.naposystems.napoleonchat.utility

sealed class StateMessage {

    data class Start(
        val messageId : Int
    ) : StateMessage()

    data class Success(
        val messageId : Int
    ) : StateMessage()

    data class Error(
        val messageId : Int
    ) : StateMessage()

}