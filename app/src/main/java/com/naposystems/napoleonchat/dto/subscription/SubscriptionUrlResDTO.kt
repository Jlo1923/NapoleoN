package com.naposystems.napoleonchat.dto.subscription

import com.naposystems.napoleonchat.model.typeSubscription.SubscriptionUrl
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionUrlResDTO(
    @Json(name = "url") val url: String
) {
    companion object{
        fun toModel(response: SubscriptionUrlResDTO): SubscriptionUrl {
            return SubscriptionUrl(
                url = response.url
            )
        }
    }
}