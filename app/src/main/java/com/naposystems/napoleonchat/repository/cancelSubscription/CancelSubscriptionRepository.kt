package com.naposystems.napoleonchat.repository.cancelSubscription

import android.content.Context
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.dto.subscription.CancelSubscriptionResDTO
import com.naposystems.napoleonchat.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class CancelSubscriptionRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractCancelSubscription.Repository {

    override suspend fun cancelSubscription(): Response<CancelSubscriptionResDTO> {
        return napoleonApi.cancelSubscription()
    }

    override suspend fun clearSubcription() {
        /*sharedPreferencesManager.reset()
        userLocalDataSource.clearAllData()

        withContext(Dispatchers.IO) {
            FileManager.deleteAllFiles(context)
        }*/
    }
}