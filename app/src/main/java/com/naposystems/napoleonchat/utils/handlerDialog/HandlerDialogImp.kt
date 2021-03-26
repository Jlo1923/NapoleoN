package com.naposystems.napoleonchat.utils.handlerDialog

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.ui.generalDialog.GeneralDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import javax.inject.Inject

class HandlerDialogImp @Inject constructor(): HandlerDialog {

    override fun generalDialog(
        title: String,
        message: String,
        isCancelable: Boolean,
        childFragmentManager: FragmentManager,
        textButtonAccept: String,
        textButtonCancel: String,
        actionAccept: () -> Unit
    ) {
        val dialog = GeneralDialogFragment.newInstance(
            title,
            message,
            isCancelable,
            textButtonAccept,
            textButtonCancel
        )
        dialog.setListener(object : GeneralDialogFragment.OnGeneralDialog {
            override fun onAccept() {
                actionAccept()
            }
        })
        dialog.show(childFragmentManager, "GeneralDialog")
    }

    override fun alertDialogWithNeutralButton(
        message: Int,
        isCancelable: Boolean,
        childFragmentManager: Context,
        titleTopButton: Int,
        titleCentralButton: Int,
        titleDownButton: Int,
        clickTopButton: (Boolean) -> Unit,
        clickDownButton: (Boolean) -> Unit
    ) {
        val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
            .setMessage(message)
            .setCancelable(isCancelable)
            .setPositiveButton(titleTopButton) { _, _ ->
                clickTopButton(true)
            }
            .setNeutralButton(titleCentralButton) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton(titleDownButton) { _, _ ->
                clickDownButton(true)
            }
            .create()

        dialog.show()

        val textColorButton =
            Utils.convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(textColorButton)
        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        positiveButton.isAllCaps = false

        val neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        neutralButton.setTextColor(textColorButton)
        neutralButton.setBackgroundColor(Color.TRANSPARENT)
        neutralButton.isAllCaps = false

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(textColorButton)
        negativeButton.setBackgroundColor(Color.TRANSPARENT)
        negativeButton.isAllCaps = false
    }

    override fun alertDialogWithoutNeutralButton(
        message: Int,
        isCancelable: Boolean,
        childFragmentManager: Context,
        location: Int,
        titlePositiveButton: Int,
        titleNegativeButton: Int,
        clickPositiveButton: (Boolean) -> Unit,
        clickNegativeButton: (Boolean) -> Unit
    ) {
        val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
            .setMessage(message)
            .setCancelable(isCancelable)
            .setPositiveButton(titlePositiveButton) { _, _ ->
                clickPositiveButton(true)
            }
            .setNegativeButton(titleNegativeButton) { dialog, _ ->
                if (location == Constants.LocationAlertDialog.CONVERSATION.location)
                    dialog.dismiss()
                else {
                    clickNegativeButton(true)
                    dialog.dismiss()
                }
            }
            .create()
        dialog.show()

        val textColorButton =
            Utils.convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(textColorButton)
        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        positiveButton.isAllCaps = false

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(textColorButton)
        negativeButton.setBackgroundColor(Color.TRANSPARENT)
        negativeButton.isAllCaps = false
    }

    override fun alertDialogInformative(
        title: String,
        message: String,
        isCancelable: Boolean,
        childFragmentManager: Context,
        titleButton: Int,
        clickTopButton: (Boolean) -> Unit
    ) {
        val dialog = AlertDialog.Builder(childFragmentManager, R.style.MyDialogTheme)
            .setMessage(message)
            .setCancelable(isCancelable)
            .setPositiveButton(titleButton) { _, _ ->
                clickTopButton(true)
            }
            .create()

        if (title.isNotEmpty()) {
            dialog.setTitle(title)
        }

        dialog.show()

        val textColorButton =
            Utils.convertAttrToColorResource(childFragmentManager, R.attr.attrTextColorButtonTint)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(textColorButton)
        positiveButton.setBackgroundColor(Color.TRANSPARENT)
        positiveButton.isAllCaps = false
    }
}