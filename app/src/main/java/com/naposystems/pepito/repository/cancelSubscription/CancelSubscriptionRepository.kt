package com.naposystems.pepito.repository.cancelSubscription

import android.content.Context
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.dto.subscription.CancelSubscriptionResDTO
import com.naposystems.pepito.ui.cancelSubscription.IContractCancelSubscription
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.webService.NapoleonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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