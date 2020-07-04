package com.naposystems.pepito.repository.notificationUtils

import com.naposystems.pepito.db.dao.contact.ContactLocalDataSource
import com.naposystems.pepito.dto.conversation.message.MessageReceivedReqDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.notificationUtils.IContractNotificationUtils
import com.naposystems.pepito.webService.NapoleonApi
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

    override fun getContactSilenced(contactId: Int, silenced : (Boolean) -> Unit) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                silenced(contactLocalDataSource.getContactSilenced(contactId))
            }
        }
    }
}