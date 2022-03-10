package com.naposystems.napoleonchat.ui.subscription

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.model.typeSubscription.SubscriptionUrl
import com.naposystems.napoleonchat.model.typeSubscription.SubscriptionUser
import com.naposystems.napoleonchat.model.typeSubscription.TypeSubscription
import com.naposystems.napoleonchat.repository.subscription.SubscriptionRepository
import com.naposystems.napoleonchat.source.remote.dto.subscription.CreateSuscriptionDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.SubscriptionUrlResDTO
import com.naposystems.napoleonchat.source.remote.dto.subscription.SubscriptionsResDTO
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository
) : ViewModel() {

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

    fun getTypeSubscription() {
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

    fun getFreeTrial(): Long {
        return repository.getFreeTrial()
    }

    /* fun getRemoteSubscription() {
        viewModelScope.launch {
            repository.getRemoteSubscription()
            getSubscription()
        }
    }*/

    /* fun getSubscription() {
        _subscriptionUser.value = repository.getSubscription()
    }*/

    fun sendPayment(typePayment: Int) {
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

    fun checkSubscription() {
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

    fun createSubscription(createSuscriptionDTO: CreateSuscriptionDTO) {

        viewModelScope.launch {
            try {

                val response = repository.createSubscription(createSuscriptionDTO)

            }catch (ex: java.lang.Exception){
                Timber.e(ex)
            }
        }

    }
    fun resetViewModel() {
        _subscriptionUrl.value = null
    }

}
