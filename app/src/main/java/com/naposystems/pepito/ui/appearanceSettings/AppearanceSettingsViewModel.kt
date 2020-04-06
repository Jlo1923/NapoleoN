package com.naposystems.pepito.ui.appearanceSettings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.repository.appearanceSettings.AppearanceSettingsRepository
import javax.inject.Inject

class AppearanceSettingsViewModel @Inject constructor(
    private val repository: AppearanceSettingsRepository
) : ViewModel(), IContractAppearanceSettings.ViewModel {

    private val _colorScheme = MutableLiveData<Int>()
    val colorScheme: LiveData<Int>
        get() = _colorScheme

    private val _userDisplayFormat = MutableLiveData<Int>()
    val userDisplayFormat: LiveData<Int>
        get() = _userDisplayFormat

    init {
        _colorScheme.value = null
        _userDisplayFormat.value = null
    }

    //region Implementation IContractAppearanceSettings.ViewModel
    override fun getColorScheme() {
        _colorScheme.value = repository.getColorScheme()
    }

    override fun getUserDisplayFormat() {
        _userDisplayFormat.value = repository.getUserDisplayFormat()
    }

    //endregion
}
