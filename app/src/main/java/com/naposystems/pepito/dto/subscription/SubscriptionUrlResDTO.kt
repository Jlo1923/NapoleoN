package com.naposystems.pepito.dto.subscription

import com.naposystems.pepito.model.typeSubscription.SubscriptionUrl
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response

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