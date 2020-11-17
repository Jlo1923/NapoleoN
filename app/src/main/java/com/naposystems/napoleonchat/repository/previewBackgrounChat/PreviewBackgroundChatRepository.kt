package com.naposystems.napoleonchat.repository.previewBackgrounChat

import com.naposystems.napoleonchat.db.dao.user.UserLocalDataSource
import com.naposystems.napoleonchat.ui.previewBackgroundChat.IContractPreviewBackgroundChat
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class PreviewBackgroundChatRepository@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSource: UserLocalDataSource
) : IContractPreviewBackgroundChat.Repository {

    override suspend fun updateChatBackground(newBackground: String) {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        userLocalDataSource.updateChatBackground(newBackground, firebaseId)
    }
}