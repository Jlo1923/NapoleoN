package com.naposystems.napoleonchat.ui.cancelSubscription

import com.naposystems.napoleonchat.source.remote.dto.subscription.CancelSubscriptionResDTO
import retrofit2.Response

interface IContractCancelSubscription {

    interface ViewModel {
        fun cancelSubscription()
    }

    interface Repository {
        suspend fun cancelSubscription(): Response<CancelSubscriptionResDTO>
        suspend fun clearSubcription()
    }
}