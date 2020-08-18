package com.naposystems.napoleonchat.ui.cancelSubscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CancelSubscriptionDialogViewModel @Inject constructor(private val repository: IContractCancelSubscription.Repository) :
    ViewModel(), IContractCancelSubscription.ViewModel {

    private val _subscriptionStatus = MutableLiveData<Int>()
    val subscriptionStatus: LiveData<Int>
        get() = _subscriptionStatus

    override fun cancelSubscription() {
        viewModelScope.launch {
            try {
                _subscriptionStatus.value = CancelSubscriptionDialogFragment.IS_SERVICE_CALLED
                val response = repository.cancelSubscription()

                if (response.isSuccessful) {
                    repository.clearSubcription()
                    _subscriptionStatus.value = CancelSubscriptionDialogFragment.SERVICE_ANSWER_OK
                } else {
                    _subscriptionStatus.value = CancelSubscriptionDialogFragment.SERVICE_ANSWER_ERROR
                }
            } catch (e: Exception) {
                _subscriptionStatus.value = CancelSubscriptionDialogFragment.SERVICE_ANSWER_ERROR
                Timber.e(e)
            }
        }
    }
}