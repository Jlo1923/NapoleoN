package com.naposystems.pepito.crypto.message

interface IContractCryptoMessage {
    fun decryptMessageBody(body: String): String
    fun encryptMessageBody(body: String): String
}