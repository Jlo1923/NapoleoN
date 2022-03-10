package com.naposystems.napoleonchat.repository.subscription

import com.naposystems.napoleonchat.source.remote.dto.contactProfile.ContactFakeResDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.CreateSuscriptionDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.StateSubscriptionResDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.SubscriptionUrlResDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.SubscriptionsResDTO
import okhttp3.ResponseBody
import retrofit2.Response

interface SubscriptionRepository {
    suspend fun getTypeSubscription(): Response<List<SubscriptionsResDTO>>
    fun getFreeTrial(): Long

    //suspend fun getRemoteSubscription()
    //fun getSubscription(): SubscriptionUser
    suspend fun sendPayment(typePayment: Int): Response<SubscriptionUrlResDTO>
    fun getSubscriptionUserError(response: ResponseBody): ArrayList<String>
    fun getError(response: ResponseBody): ArrayList<String>
    fun getSubscriptionUrlError(response: ResponseBody): ArrayList<String>
    suspend fun checkSubscription(): Response<StateSubscriptionResDTO>
    suspend fun createSubscription(createSuscriptionDTO: CreateSuscriptionDTO): Response<CreateSuscriptionDTO>
}