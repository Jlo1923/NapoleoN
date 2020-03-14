package com.naposystems.pepito.dto.subscription

import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionUserResDTO (
    @Json(name = "id") val id: Int = 0,
    @Json(name = "user_id") val userId: Int = 0,
    @Json(name = "subscription_id") val subscriptionId: Int = 0,
    @Json(name = "state") val state: Int = 0,
    @Json(name = "date_approved") val dateApproved: Long = 0L,
    @Json(name = "date_expires") val dateExpires: Long = 0L,
    @Json(name = "paypal_id") val paypalId: String = "",
    @Json(name = "token_paypal") val tokenPaypal: String = "",
    @Json(name = "payment_method") val paymentMethod: String = ""
) {
    companion object{
        fun toModel(response: SubscriptionUserResDTO): SubscriptionUser{
            return SubscriptionUser(
                subscriptionId = response.subscriptionId,
                dateExpires = response.dateExpires
            )
        }
    }
}