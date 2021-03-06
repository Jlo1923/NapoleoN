package com.naposystems.napoleonchat.utility

import com.naposystems.napoleonchat.app.NapoleonApplication

enum class StatusCallEnum {
    STATUS_NO_CALL,
    STATUS_CONNECTED_CALL
}

fun StatusCallEnum.isNoCall(): Boolean =
    NapoleonApplication.statusCall == StatusCallEnum.STATUS_NO_CALL

fun StatusCallEnum.isConnectedCall(): Boolean =
    NapoleonApplication.statusCall == StatusCallEnum.STATUS_CONNECTED_CALL

enum class TypeEndCallEnum{
    TYPE_CANCEL,
    TYPE_REJECT
}