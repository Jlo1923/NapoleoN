package com.naposystems.pepito.webService.service

import org.json.JSONObject

interface IContractSocketService {

    fun subscribe(jsonObject: JSONObject)

    fun unSubscribe(jsonObject: JSONObject, channelName: String)
}