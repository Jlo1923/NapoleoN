package com.naposystems.pepito.ui.baseFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseViewModel @Inject constructor(
    private val repository: IContractBase.Repository
) : ViewModel(), IContractBase.ViewModel {

    override fun outputControl(state: Int) {
        viewModelScope.launch {
            repository.outputControl(state)
        }
    }
}
