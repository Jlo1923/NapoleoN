package com.naposystems.pepito.utility.sharedViewModels.timeFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.pepito.repository.timeFormat.TimeFormatRepository
import com.naposystems.pepito.ui.timeFormat.IContractTimeFormat
import javax.inject.Inject

class TimeFormatShareViewModel @Inject constructor(
    private val repository: TimeFormatRepository
) : ViewModel(), IContractTimeFormat.ViewModel {

    private val _timeFormat = MutableLiveData<Int>()
    val timeFormat: LiveData<Int>
        get() = _timeFormat

    override fun setTimeFormat(format: Int) {
        _timeFormat.value = format
        repository.setTimeFormat(format)
    }

    override fun getTimeFormat() {
        _timeFormat.value = repository.getTimeFormat()
    }

}