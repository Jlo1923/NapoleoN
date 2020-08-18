package com.naposystems.napoleonchat.repository.notificationUtils

import com.naposystems.napoleonchat.db.dao.contact.ContactLocalDataSource
import com.naposystems.napoleonchat.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.napoleonchat.webService.NapoleonApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class NotificationUtilsRepository @Inject constructor(
    private val napoleonApi: NapoleonApi,
    private val contactLocalDataSource: ContactLocalDataSource,
    private val sharedPreferencesManager: SharedPreferencesManager
) :
    IContractNotificationUtils.Repository {

    override fun notifyMessageReceived(messageId: String) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val messageReceivedReqDTO = MessageReceivedReqDTO(messageId)
                    napoleonApi.notifyMessageReceived(messageReceivedReqDTO)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun getIsOnCallPref() =
        sharedPreferencesManager.getBoolean(Constants.SharedPreferences.PREF_IS_ON_CALL, false)

    override fun getContactSilenced(contactId: Int, silenced : (Boolean?) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                silenced(contactLocalDataSource.getContactSilenced(contactId))
            }
        }
    }
}