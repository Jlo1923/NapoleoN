package com.naposystems.pepito.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.dto.subscription.SubscriptionUrlResDTO
import com.naposystems.pepito.dto.subscription.SubscriptionsResDTO
import com.naposystems.pepito.model.typeSubscription.SubscriptionUrl
import com.naposystems.pepito.model.typeSubscription.SubscriptionUser
import com.naposystems.pepito.model.typeSubscription.TypeSubscription
import com.naposystems.pepito.repository.subscription.SubscriptionRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel(), IContractSubscription.ViewModel {

    private val _typeSubscription = MutableLiveData<List<TypeSubscription>>()
    val typeSubscription: LiveData<List<TypeSubscription>>
        get() = _typeSubscription

    private val _subscriptionUrl = MutableLiveData<SubscriptionUrl>()
    val subscriptionUrl: LiveData<SubscriptionUrl>
        get() = _subscriptionUrl


    private val _subscriptionUser = MutableLiveData<SubscriptionUser>()
    val subscriptionUser: LiveData<SubscriptionUser>
        get() = _subscriptionUser

    private val _subscriptionUserError = MutableLiveData<List<String>>()
    val subscriptionUserError: LiveData<List<String>>
        get() = _subscriptionUserError

    private val _getTypeSubscriptionError = MutableLiveData<List<String>>()
    val getTypeSubscriptionError: LiveData<List<String>>
        get() = _getTypeSubscriptionError

    private val _sendPaymentError = MutableLiveData<List<String>>()
    val sendPaymentError: LiveData<List<String>>
        get() = _sendPaymentError

    private val _subscriptionState = MutableLiveData<String>()
    val subscriptionState: LiveData<String>
        get() = _subscriptionState

    override fun getTypeSubscription() {
        viewModelScope.launch {
            try {
                val response = repository.getTypeSubscription()
                if (response.isSuccessful) {
                    _typeSubscription.value =
                        SubscriptionsResDTO.toListSubscriptions(response.body()!!)
                } else {
                    _getTypeSubscriptionError.value = repository.getError(response.errorBody()!!)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun getFreeTrial(): Long {
        return repository.getFreeTrial()
    }

    override fun getRemoteSubscription() {
        viewModelScope.launch {
            repository.getRemoteSubscription()
            getSubscription()
        }
    }

    override fun getSubscription() {
        _subscriptionUser.value = repository.getSubscription()
    }

    override fun sendPayment(typePayment: Int) {
        viewModelScope.launch {
            try {
                val response = repository.sendPayment(typePayment)

                if (response.isSuccessful) {
                    _subscriptionUrl.value = SubscriptionUrlResDTO.toModel(response.body()!!)
                } else {
                    _sendPaymentError.value =
                        repository.getSubscriptionUrlError(response.errorBody()!!)
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun checkSubscription() {
        viewModelScope.launch {
            try {
                val response = repository.checkSubscription()

                if (response.isSuccessful) {
                    response.body()?.let {
                        _subscriptionState.value = it.state
                    }
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun resetViewModel() {
        _subscriptionUrl.value = null
    }
}
