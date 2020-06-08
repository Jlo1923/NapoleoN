package com.naposystems.pepito.ui.enterPin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.pepito.utility.Constants
import kotlinx.coroutines.launch
import javax.inject.Inject

class EnterPinViewModel @Inject constructor(
    private val repository: IContractEnterPin.Repository
) : ViewModel(), IContractEnterPin.ViewModel {

    private val _attempts = MutableLiveData<Int>()
    val attempts: LiveData<Int>
        get() = _attempts

    private val _totalAttempts = MutableLiveData<Int>()
    val totalAttempts: LiveData<Int>
        get() = _totalAttempts

    private val _validPassword = MutableLiveData<Boolean>()
    val validPassword: LiveData<Boolean>
        get() = _validPassword

    private val _biometricsOption = MutableLiveData<Int>()
    val biometricsOption: LiveData<Int>
        get() = _biometricsOption

    override fun validatedAccessPin(pin: String) {
        viewModelScope.launch {
            val pinUser = repository.getAccessPin().accessPin

            _attempts.value = repository.getAttempts()

            if (pinUser == pin) {
                _validPassword.value = true
                _attempts.value = 0
                repository.setAttempts(0)
                repository.setTotalAttempts(0)
                repository.setLockStatus(Constants.LockStatus.UNLOCK.state)
            }
            else {
                _validPassword.value = false
                _attempts.value = _attempts.value!!.inc()
                repository.setAttempts(_attempts.value!!)
                if (_attempts.value == 3) {
                    _totalAttempts.value = repository.getTotalAttempts()
                    _totalAttempts.value = _totalAttempts.value!!.inc()
                    when (_totalAttempts.value) {
                        Constants.TotalAttempts.ATTEMPTS_ONE.attempts -> {
                            repository.setUnlockAppTime(
                                System.currentTimeMillis().plus(
                                    Constants.TimeUnlockApp.THIRTY_SECONDS.time
                                )
                            )
                        }
                        Constants.TotalAttempts.ATTEMPTS_TWO.attempts -> {
                            repository.setUnlockAppTime(
                                System.currentTimeMillis().plus(
                                    Constants.TimeUnlockApp.FIVE_MINUTES.time
                                )
                            )
                        }
                        Constants.TotalAttempts.ATTEMPTS_THREE.attempts -> {
                            repository.setUnlockAppTime(
                                System.currentTimeMillis().plus(
                                    Constants.TimeUnlockApp.TWENTY_MINUTES.time
                                )
                            )
                        }
                        Constants.TotalAttempts.ATTEMPTS_FOUR.attempts -> {
                            repository.setUnlockAppTime(
                                System.currentTimeMillis().plus(
                                    Constants.TimeUnlockApp.ONE_HOUR.time
                                )
                            )
                        }
                        else -> {
                            repository.setUnlockAppTime(
                                System.currentTimeMillis().plus(
                                    Constants.TimeUnlockApp.ONE_DAY.time
                                )
                            )
                        }
                    }
                    repository.setTotalAttempts(_totalAttempts.value!!)
                    repository.setLockType(Constants.LockTypeApp.LOCK_APP_FOR_ATTEMPTS.type)
                }
            }
        }
    }

    override fun getAttempts() {
        viewModelScope.launch {
            _attempts.value = repository.getAttempts()
        }
    }

    override fun setAttempts(attempts: Int) {
        viewModelScope.launch {
            repository.setAttempts(attempts)
        }
    }

    override fun setTotalAttempts(attempts: Int) {
        viewModelScope.launch {
            repository.setTotalAttempts(attempts)
        }
    }

    override fun setLockStatus(state: Int) {
        viewModelScope.launch {
            repository.setLockStatus(state)
        }
    }

    override fun getBiometricsOption() {
        viewModelScope.launch {
            _biometricsOption.value = repository.getBiometricsOption()
        }
    }

    override fun setBiometricPreference(option: Int) {
        repository.setBiometricPreference(option)
    }
}
