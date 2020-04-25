package com.naposystems.pepito.ui.timeFormat

interface IContractTimeFormat {

    interface ViewModel {
        fun setTimeFormat(format: Int)
        fun getTimeFormat()
    }

    interface Repository {
        fun setTimeFormat(format: Int)
        fun getTimeFormat(): Int
    }
}