package com.naposystems.napoleonchat.repository.previewBackgrounChat

import com.naposystems.napoleonchat.source.local.datasource.user.UserLocalDataSourceImp
import com.naposystems.napoleonchat.ui.previewBackgroundChat.IContractPreviewBackgroundChat
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import javax.inject.Inject

class PreviewBackgroundChatRepository@Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
    private val userLocalDataSourceImp: UserLocalDataSourceImp
) : IContractPreviewBackgroundChat.Repository {

    override suspend fun updateChatBackground(newBackground: String) {
        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")
        userLocalDataSourceImp.updateChatBackground(newBackground, firebaseId)
    }
}