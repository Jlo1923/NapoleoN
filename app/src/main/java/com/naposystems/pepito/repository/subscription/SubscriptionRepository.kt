package com.naposystems.pepito.repository.subscription

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.subscription.*
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import com.naposystems.pepito.ui.subscription.IContractSubscription
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
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

    override fun getSubscription(): SubscriptionUser {
        return SubscriptionUser(
            sharedPreferencesManager.getInt(
                Constants.SharedPreferences.PREF_TYPE_SUBSCRIPTION
            ),
            sharedPreferencesManager.getLong(
                Constants.SharedPreferences.PREF_SUBSCRIPTION_TIME
            )
        )
    }

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
}