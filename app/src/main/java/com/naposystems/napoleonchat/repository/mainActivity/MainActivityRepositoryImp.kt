package com.naposystems.napoleonchat.repository.mainActivity

import android.net.Uri
import com.naposystems.napoleonchat.service.socketClient.SocketClient
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_JSON_NOTIFICATION
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_LAST_JSON_NOTIFICATION
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class MainActivityRepositoryImp @Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketClient: SocketClient
) : MainActivityRepository {

    override suspend fun getUser(): UserEntity {
        return userLocalDataSourceImp.getMyUser()
    }

    override suspend fun getAccountStatus(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_ACCOUNT_STATUS)
    }

    override fun getOutputControl(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_OUTPUT_CONTROL)
    }

    override suspend fun setOutputControl(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_OUTPUT_CONTROL, state
        )
    }

    override fun getTimeRequestAccessPin(): Int {
        return sharedPreferencesManager.getInt(
            Constants.SharedPreferences.PREF_TIME_REQUEST_ACCESS_PIN
        )
    }

    override fun setLockTimeApp(lockTime: Long) {
        sharedPreferencesManager.putLong(
            Constants.SharedPreferences.PREF_LOCK_TIME_APP, lockTime
        )
    }

    override fun setJsonNotification(json: String) {
        if (sharedPreferencesManager.getString(PREF_LAST_JSON_NOTIFICATION, "") != json
            && sharedPreferencesManager.getString(PREF_JSON_NOTIFICATION, "") != json
        ) {
            sharedPreferencesManager.putString(
                PREF_LAST_JSON_NOTIFICATION, json
            )
            sharedPreferencesManager.putString(
                PREF_JSON_NOTIFICATION, json
            )
        }
    }

    override suspend fun setLockStatus(state: Int) {
        sharedPreferencesManager.putInt(
            Constants.SharedPreferences.PREF_LOCK_STATUS, state
        )
    }

    override suspend fun getLockTimeApp(): Long {
        return sharedPreferencesManager.getLong(
            Constants.SharedPreferences.PREF_LOCK_TIME_APP
        )
    }

    override suspend fun getContactById(contactId: Int) =
        contactLocalDataSource.getContactById(contactId)

    override fun getRecoveryQuestionsPref(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
    }

    override fun disconnectSocket() {
//            socketClient.disconnectSocket()
    }

    override fun addUriListToCache(listOf: List<Uri>) {
        sharedPreferencesManager.puStringSet("test", listOf)
    }

    fun getPendingUris(): List<Uri> {
        val urisString = sharedPreferencesManager.getStringSet("test")
        val listString = urisString?.toList()
        val listUris = listString?.map { Uri.parse(it) }
        return listUris ?: emptyList()
    }

    override fun removeUriListCache() {
        sharedPreferencesManager.removeSetIdsToRemove()
    }
}