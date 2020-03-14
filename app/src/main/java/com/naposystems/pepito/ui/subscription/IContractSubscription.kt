package com.naposystems.pepito.ui.subscription

import com.naposystems.pepito.dto.subscription.SubscriptionUrlResDTO
import com.naposystems.pepito.dto.subscription.SubscriptionUserResDTO
import com.naposystems.pepito.dto.subscription.SubscriptionsResDTO
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractSubscription {
    interface ViewModel {
        fun getTypeSubscription()
        fun getFreeTrial(): Long
        fun sendPayment(typePayment: Int)
        fun getSubscription(): SubscriptionUser
        fun resetViewModel()
    }

    interface Repository {
        suspend fun getTypeSubscription(): Response<List<SubscriptionsResDTO>>
        fun getFreeTrial(): Long
        fun getSubscription(): SubscriptionUser
        suspend fun sendPayment(typePayment: Int): Response<SubscriptionUrlResDTO>
        fun getSubscriptionUserError(response: ResponseBody): ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
        fun getSubscriptionUrlError(response: ResponseBody): ArrayList<String>
    }
}