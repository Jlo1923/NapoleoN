package com.naposystems.napoleonchat.dialog.cancelSubscription

import android.content.Context
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.remote.api.NapoleonApi
import com.naposystems.napoleonchat.source.remote.dto.subscription.CancelSubscriptionResDTO
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import retrofit2.Response
import javax.inject.Inject

class CancelSubscriptionDialogRepositoryImp @Inject constructor(
    private val context: Context,
    private val napoleonApi: NapoleonApi,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager
) : CancelSubscriptionDialogRepository {

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