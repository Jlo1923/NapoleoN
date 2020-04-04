package com.naposystems.pepito.repository.previewBackgrounChat

import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import com.naposystems.pepito.ui.previewBackgroundChat.IContractPreviewBackgroundChat
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
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