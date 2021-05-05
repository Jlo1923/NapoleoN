package com.naposystems.napoleonchat.dialog.cancelSubscription

import com.naposystems.napoleonchat.source.remote.dto.subscription.CancelSubscriptionResDTO
import retrofit2.Response

interface CancelSubscriptionDialogRepository {
    suspend fun cancelSubscription(): Response<CancelSubscriptionResDTO>
    suspend fun clearSubcription()
}