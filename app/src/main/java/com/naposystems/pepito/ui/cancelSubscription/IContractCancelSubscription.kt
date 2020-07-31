package com.naposystems.pepito.ui.cancelSubscription

import com.naposystems.pepito.dto.subscription.CancelSubscriptionResDTO
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