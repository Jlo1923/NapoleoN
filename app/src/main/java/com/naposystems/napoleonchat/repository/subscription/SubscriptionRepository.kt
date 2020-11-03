package com.naposystems.napoleonchat.repository.subscription

import com.naposystems.napoleonchat.dto.subscription.*
import com.naposystems.napoleonchat.model.typeSubscription.SubscriptionUser
import com.naposystems.napoleonchat.ui.subscription.IContractSubscription
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SubscriptionRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : IContractSubscription.Repository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override fun getFreeTrial(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL
        )
    }

    /*override suspend fun getRemoteSubscription() {
        val response = napoleonApi.getSubscriptionUser()
        response.body()?.let { responseBody ->
            val currentTimeSubscriptionLocal = sharedPreferencesManager.getLong(
                Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME
            )
            if (System.currentTimeMillis() > currentTimeSubscriptionLocal &&
                response.isSuccessful && responseBody.subscriptionId != 0
            ) {
                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_TYPE_SUBSCRIPTION,
                    responseBody.subscriptionId
                )
                sharedPreferencesManager.putLong(
                    Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME,
                    TimeUnit.SECONDS.toMillis(responseBody.dateExpires)
                )
            }
        }
    }*/

    /*override fun getSubscription(): SubscriptionUser {
        val subscriptionTime = sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME
        )
        if (subscriptionTime != 0L) {
            sharedPreferencesManager.putLong(
                Constants.SharedPreferences.PREF_FREE_TRIAL, 0L
            )
        }

        return SubscriptionUser(
            sharedPreferencesManager.getInt(
                Constants.SharedPreferences.PREF_TYPE_SUBSCRIPTION
            ),
            subscriptionTime
        )
    }*/

    override suspend fun getTypeSubscription(): Response<List<SubscriptionsResDTO>> {
        return napoleonApi.typeSubscriptions()
    }

    override suspend fun sendPayment(typePayment: Int): Response<SubscriptionUrlResDTO> {
        val typeSubscriptionsResDTO = TypeSubscriptionReqDTO(typePayment)

        return napoleonApi.sendSelectedSubscription(typeSubscriptionsResDTO)
    }

    override fun getSubscriptionUserError(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(SubscriptionUserErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()

        errorList.add(error!!.error)
        return errorList
    }

    override fun getError(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(SubscriptionsErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()

        errorList.add(error!!.error)
        return errorList
    }

    override fun getSubscriptionUrlError(response: ResponseBody): ArrayList<String> {
        val adapter = moshi.adapter(SubscriptionUrlErrorDTO::class.java)
        val error = adapter.fromJson(response.string())
        val errorList = ArrayList<String>()

        errorList.add(error!!.error)
        return errorList
    }

    override suspend fun checkSubscription(): Response<StateSubscriptionResDTO> {
        return napoleonApi.checkSubscription()
    }
}