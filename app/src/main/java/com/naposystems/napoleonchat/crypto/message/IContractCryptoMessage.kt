package com.naposystems.napoleonchat.crypto.message

interface IContractCryptoMessage {
    fun decryptMessageBody(body: String): String
    fun encryptMessageBody(body: String): String
}