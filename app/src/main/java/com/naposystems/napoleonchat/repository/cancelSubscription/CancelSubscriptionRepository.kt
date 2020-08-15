package com.naposystems.napoleonchat.repository.cancelSubscription

import android.content.Context
import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.dto.subscription.CancelSubscriptionResDTO
import com.naposystems.napoleonchat.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.webService.NapoleonApi
import retrofit2.Response
import javax.inject.Inject

class CancelSubscriptionRepository @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSource: UserLocalDataSource,
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