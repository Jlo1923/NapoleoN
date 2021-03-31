package com.naposystems.napoleonchat.service.notificationClient

import com.naposystems.napoleonchat.model.CallModel

interface HandlerNotificationCall {
    fun handlerCall(callModel: CallModel)
}