package com.naposystems.pepito.ui.subscription

import com.naposystems.pepito.dto.subscription.StateSubscriptionResDTO
import com.naposystems.pepito.dto.subscription.SubscriptionUrlResDTO
import com.naposystems.pepito.dto.subscription.SubscriptionsResDTO
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import okhttp3.ResponseBody
import retrofit2.Response

interface IContractSubscription {
    interface ViewModel {
        fun getTypeSubscription()
        fun getFreeTrial(): Long
        fun getRemoteSubscription()
        fun getSubscription()
        fun sendPayment(typePayment: Int)
        fun checkSubscription()
        fun resetViewModel()
    }

    interface Repository {
        suspend fun getTypeSubscription(): Response<List<SubscriptionsResDTO>>
        fun getFreeTrial(): Long
        suspend fun getRemoteSubscription()
        fun getSubscription(): SubscriptionUser
        suspend fun sendPayment(typePayment: Int): Response<SubscriptionUrlResDTO>
        fun getSubscriptionUserError(response: ResponseBody): ArrayList<String>
        fun getError(response: ResponseBody): ArrayList<String>
        fun getSubscriptionUrlError(response: ResponseBody): ArrayList<String>
        suspend fun checkSubscription(): Response<StateSubscriptionResDTO>
    }
}