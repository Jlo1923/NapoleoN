package com.naposystems.napoleonchat.repository.subscription

import android.util.Log
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.subscription.*
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.squareup.moshi.Moshi
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class SubscriptionRepositoryImp
@Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val sharedPreferencesManager: SharedPreferencesManager
) : SubscriptionRepository {

    private val moshi: Moshi by lazy {
        Moshi.Builder().build()
    }

    override fun getFreeTrial(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_FREE_TRIAL
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

    override suspend fun checkSubscription(): Response<StateSubscriptionResDTO> {
        return napoleonApi.checkSubscription()
    }


    override suspend fun createSubscription(createSuscriptionDTO: CreateSuscriptionDTO): Response<CreateSuscriptionDTO> {
        Log.e("api", createSuscriptionDTO.user_id)
        return napoleonApi.createSubscription(createSuscriptionDTO)
    }


}