package com.naposystems.napoleonchat.repository.mainActivity

import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.source.local.datasource.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.source.local.entity.UserEntity
import com.naposystems.napoleonchat.ui.mainActivity.IContractMainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_JSON_NOTIFICATION
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_LAST_JSON_NOTIFICATION
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.service.socketMessage.SocketMessageService
import javax.inject.Inject

class MainActivityRepository @Inject constructor(
    private val contactLocalDataSource: ContactLocalDataSource,
    private val userLocalDataSourceImp: UserLocalDataSourceImp,
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val socketMessageService: SocketMessageService
) :    IContractMainActivity.Repository {

    override suspend fun getUser(): UserEntity {
//        val firebaseId = sharedPreferencesManager
//            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
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
            && sharedPreferencesManager.getString(PREF_JSON_NOTIFICATION, "") != json) {
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

    override fun resetIsOnCallPref() {
        NapoleonApplication.isOnCall = false
    }

    override fun getRecoveryQuestionsPref(): Int {
        return sharedPreferencesManager.getInt(Constants.SharedPreferences.PREF_RECOVERY_QUESTIONS_SAVED)
    }

    override fun disconnectSocket() {
//        socketMessageService.disconnectSocket()
    }
}