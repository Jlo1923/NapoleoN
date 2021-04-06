package com.naposystems.napoleonchat.ui.notificationSetting

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