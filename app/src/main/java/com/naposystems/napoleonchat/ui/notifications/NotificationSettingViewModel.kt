package com.naposystems.napoleonchat.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class NotificationSettingViewModel @Inject constructor(
    private val repository: IContractNotificationSetting.Repository
) : ViewModel(), IContractNotificationSetting.ViewModel {

    /*private val _notificationMessageChannelId = MutableLiveData<Int>()
    val notificationMessageChannelId: LiveData<Int>
        get() = _notificationMessageChannelId*/

    /*override fun getNotificationMessageChannelId() {
        _notificationMessageChannelId.value = repository.getNotificationMessageChannelId()
    }*/
}