package com.naposystems.napoleonchat.ui.colorScheme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naposystems.napoleonchat.repository.colorScheme.ColorSchemeRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class ColorSchemeViewModel @Inject constructor(private val repository: ColorSchemeRepository) :
    ViewModel(), IContractColorScheme.ViewModel {

    private val _theme = MutableLiveData<Int>()
    val theme: LiveData<Int>
        get() = _theme

    init {
        getActualTheme()
    }

    //region Implementation IContractColorScheme.ViewModel
    override fun getActualTheme() {
        _theme.value = repository.getActualTheme()
    }

    override fun setTheme(theme: Int) {
        _theme.value = theme
    }

    override fun saveTheme(newTheme: Int) {
        viewModelScope.launch {
            repository.saveTheme(newTheme)
            _theme.value = newTheme
        }
    }
    //endregion
}