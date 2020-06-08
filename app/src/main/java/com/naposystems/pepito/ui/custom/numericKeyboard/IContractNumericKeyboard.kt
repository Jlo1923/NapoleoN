package com.naposystems.pepito.ui.custom.numericKeyboard

interface IContractNumericKeyboard {

    fun setListener(listener: NumericKeyboardCustomView.OnEventListener)

    fun showDeleteKey(show: Boolean)
}