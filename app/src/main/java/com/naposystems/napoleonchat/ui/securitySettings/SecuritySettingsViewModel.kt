package com.naposystems.napoleonchat.ui.securitySettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.utility.Constants
import javax.inject.Inject

class SecuritySettingsViewModel @Inject constructor(
    private val repository: IContractSecuritySettings.Repository
) : ViewModel(), IContractSecuritySettings.ViewModel {

    private val _allowDownloads = MutableLiveData<Int>()
    val allowDownloads: LiveData<Int>
        get() = _allowDownloads

    private val _biometricsOption = MutableLiveData<Int>()
    val biometricsOption: LiveData<Int>
        get() = _biometricsOption

    private val _timeRequestAccessPin = MutableLiveData<Int>()
    val timeRequestAccessPin: LiveData<Int>
        get() = _timeRequestAccessPin

    //region Implementation IContractSecuritySettings.ViewModel
    override fun getAllowDownload() {
        _allowDownloads.value = repository.getAllowDownload()
    }

    override fun updateAllowDownload(state: Boolean) {
        val newState = if (state) Constants.AllowDownloadAttachments.YES.option
        else Constants.AllowDownloadAttachments.NO.option

        repository.updateAllowDownload(newState)
        _allowDownloads.value = newState
    }

    override fun getBiometricsOption() {
        _biometricsOption.value = repository.getBiometricsOption()
    }

    override fun getTimeRequestAccessPin() {
        _timeRequestAccessPin.value = repository.getTimeRequestAccessPin()
    }

    //endregion
}
