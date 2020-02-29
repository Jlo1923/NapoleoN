package com.naposystems.pepito.ui.securitySettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.utility.Constants
import javax.inject.Inject

class SecuritySettingsViewModel @Inject constructor(
    private val repository: IContractSecuritySettings.Repository
) : ViewModel(), IContractSecuritySettings.ViewModel {

    private val _selfDestructTime = MutableLiveData<Int>()
    val selfDestructTime: LiveData<Int>
        get() = _selfDestructTime

    private val _allowDownloads = MutableLiveData<Int>()
    val allowDownloads: LiveData<Int>
        get() = _allowDownloads

    private val _messageSelfDestructTimeNotSent = MutableLiveData<Int>()
    val messageSelfDestructTimeNotSent: LiveData<Int>
        get() = _messageSelfDestructTimeNotSent

    private val _biometricsOption = MutableLiveData<Int>()
    val biometricsOption: LiveData<Int>
        get() = _biometricsOption

    private val _timeRequestAccessPin = MutableLiveData<Int>()
    val timeRequestAccessPin: LiveData<Int>
        get() = _timeRequestAccessPin

    //region Implementation IContractSecuritySettings.ViewModel
    override fun getSelfDestructTime() {
        _selfDestructTime.value = repository.getSelfDestructTime()
    }

    override fun getAllowDownload() {
        _allowDownloads.value = repository.getAllowDownload()
    }

    override fun updateAllowDownload(state: Boolean) {
        val newState = if (state) Constants.AllowDownloadAttachments.YES.option
        else Constants.AllowDownloadAttachments.NO.option

        repository.updateAllowDownload(newState)
        _allowDownloads.value = newState
    }

    override fun getMessageSelfDestructTimeNotSent() {
        _messageSelfDestructTimeNotSent.value = repository.getMessageSelfDestructTimeNotSent()
    }

    override fun getBiometricsOption() {
        _biometricsOption.value = repository.getBiometricsOption()
    }

    override fun getTimeRequestAccessPin() {
        _timeRequestAccessPin.value = repository.getTimeRequestAccessPin()
    }

    //endregion
}
