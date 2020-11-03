package com.naposystems.napoleonchat.utility

class Data {
    companion object {
        var isGeneralChannelSubscribed: Boolean = false
        var contactId: Int = 0
        var isOnCall: Boolean = false
        var currentCallContactId: Int = 0
        var isContactReadyForCall: Boolean =
            false /*Esto es usado para que solo llame el servicio una vez ya que el usuario se desconecta y se vuelve a conectar más adelante*/
    }
}