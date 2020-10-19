package com.naposystems.napoleonchat.utility.sharedViewModels.defaulPreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultPreferencesViewModel @Inject constructor(
    private val repository : IContractDefaultPreferences.Repository
) : ViewModel(), IContractDefaultPreferences.ViewModel {

    override fun setDefaultPreferences() {
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