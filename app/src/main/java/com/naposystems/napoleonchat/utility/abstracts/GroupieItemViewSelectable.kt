package com.naposystems.napoleonchat.utility.abstracts

import androidx.viewbinding.ViewBinding
import com.xwray.groupie.viewbinding.BindableItem

abstract class GroupieItemViewSelectable<T : ViewBinding> : BindableItem<T>() {

    var isSelected: Boolean = false
        set(value) {
            field = value
            selectionResolver()
        }

    fun selectionResolver() {
        if (isSelected) {
            changeToSelected()
        } else {
            changeToUnselected()
        }
    }

    abstract fun changeToSelected()

    abstract fun changeToUnselected()
}