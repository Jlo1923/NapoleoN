package com.naposystems.napoleonchat.utility.sharedViewModels.timeFormat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naposystems.napoleonchat.repository.timeFormat.TimeFormatRepository
import com.naposystems.napoleonchat.ui.timeFormat.IContractTimeFormat
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

    override fun getValTimeFormat(): Int? {
        return timeFormat.value
    }

}