package com.naposystems.napoleonchat.ui.napoleonKeyboard

interface IContractNapoleonKeyboard {

    fun toggle(keyboardHeight: Int)
    fun isShowing(): Boolean
    fun handleBackButton()
    fun dispose()
}