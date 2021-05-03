package com.naposystems.napoleonchat.service.notificationClient

interface HandlerNotificationCall {
    fun handlerCall(dataFromNotification: Map<String, String>)
}