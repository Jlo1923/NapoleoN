package com.naposystems.pepito.dto.subscription

import com.naposystems.pepito.model.typeSubscription.TypeSubscription
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionsResDTO(
    @Json(name = "id") val subscriptionId: Int,
    @Json(name = "description") val description: String,
    @Json(name = "type") val type: Int,
    @Json(name = "quantity") val quantity: Int,
    @Json(name = "price") val price: Double
) {
    companion object {
        fun toListSubscriptions(listTypeSubscription: List<SubscriptionsResDTO>): List<TypeSubscription> {
            val listSubscription: MutableList<TypeSubscription> = ArrayList()

            for (subscription in listTypeSubscription) {
                listSubscription.add(
                    toSubscription(
                        subscription
                    )
                )
            }
            return listSubscription
        }

        private fun toSubscription(subscriptionsResDTO: SubscriptionsResDTO): TypeSubscription {
            return TypeSubscription(
                id = subscriptionsResDTO.subscriptionId,
                description = subscriptionsResDTO.description,
                type = subscriptionsResDTO.type,
                quantity = subscriptionsResDTO.quantity,
                price = subscriptionsResDTO.price
            )
        }
    }
}