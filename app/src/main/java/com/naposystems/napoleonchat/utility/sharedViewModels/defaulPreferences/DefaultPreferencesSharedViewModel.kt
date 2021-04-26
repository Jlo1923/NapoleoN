package com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultPreferencesSharedViewModel @Inject constructor(
    private val repository: DefaultPreferencesSharedRepository
) : ViewModel() {

    fun setDefaultPreferences() {
        viewModelScope.launch {
            repository.setDefaultTheme()
            repository.setDefaultUserDisplayFormat()
            repository.setDefaultTimeFormat()
            repository.setDefaultSelfDestructTime()
            repository.setDefaultTimeRequestAccessPin()
            repository.setDefaultAllowDownloadAttachments()
            repository.setDefaultLockType()
            repository.setDefaultSelfDestructTimeMessageNotSent()
            repository.setDefaultAttemptsForRetryCode()
            repository.setDefaultTimeForRetryCode()
            repository.setDefaultAttemptsForNewCode()
        }
    }

}