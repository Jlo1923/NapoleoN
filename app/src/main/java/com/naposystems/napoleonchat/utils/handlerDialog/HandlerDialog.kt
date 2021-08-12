package com.naposystems.napoleonchat.utils.handlerDialog

import android.content.Context
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

interface HandlerDialog {

    fun generalDialog(
        title: String,
        message: String,
        isCancelable: Boolean,
        childFragmentManager: FragmentManager,
        textButtonAccept: String = "",
        textButtonCancel: String = "",
        actionAccept: () -> Unit
    ): DialogFragment

    fun alertDialogWithNeutralButton(
        message: Int,
        isCancelable: Boolean,
        childFragmentManager: Context,
        titleTopButton: Int,
        titleCentralButton: Int,
        titleDownButton: Int,
        clickTopButton: (Boolean) -> Unit,
        clickDownButton: (Boolean) -> Unit
    )

    fun alertDialogWithoutNeutralButton(
        message: Int,
        isCancelable: Boolean,
        childFragmentManager: Context,
        location: Int,
        titlePositiveButton: Int,
        titleNegativeButton: Int,
        clickPositiveButton: (Boolean) -> Unit,
        clickNegativeButton: (Boolean) -> Unit
    )

    fun alertDialogInformative(
        title: String,
        message: String,
        isCancelable: Boolean,
        childFragmentManager: Context,
        titleButton: Int,
        clickTopButton: (Boolean) -> Unit
    )
}