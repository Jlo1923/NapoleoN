package com.naposystems.pepito.ui.custom.emptyState

interface IContractEmptyState {

    fun setImageEmptyState(drawable: Int)
    fun setTitleEmptyState(string: Int)
    fun setDescriptionEmptyState(string: Int)
    fun imageViewSetVisibility(isVisible: Boolean)
    fun textViewTitleSetVisibility(isVisible: Boolean)
    fun textViewDescriptionSetVisibility(isVisible: Boolean)
}