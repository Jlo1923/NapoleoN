package com.naposystems.pepito.utility.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton

import com.naposystems.pepito.R

private const val ICON_DRAWABLE = "ICON_DRAWABLE"
private const val MESSAGE = "MESSAGE"

class PermissionDialogFragment : DialogFragment() {
    private var iconDrawable: Int? = null
    private var message: String? = null
    private var listener: OnDialogListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            iconDrawable = it.getInt(ICON_DRAWABLE)
            message = it.getString(MESSAGE)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.fragment_permission_dialog, null)

        val alert = AlertDialog.Builder(requireContext())
            .setView(view)

        val dialog = alert.create()

        val imageViewIcon = view?.findViewById<ImageView>(R.id.imageView_icon)
        val textViewMessage = view?.findViewById<TextView>(R.id.textView_message)
        val buttonAccept = view?.findViewById<MaterialButton>(R.id.button_accept)
        val buttonCancel = view?.findViewById<MaterialButton>(R.id.button_cancel)

        imageViewIcon?.setImageDrawable(
            requireContext().resources.getDrawable(
                iconDrawable!!,
                requireContext().theme
            )
        )
        textViewMessage?.text = message

        buttonAccept!!.setOnClickListener {
            listener?.onAcceptPressed()
            dialog.dismiss()
        }

        buttonCancel!!.setOnClickListener {
            listener?.onCancelPressed()
            dialog.dismiss()
        }

        return dialog
    }

    fun setListener(listener: OnDialogListener) {
        this.listener = listener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

    override fun onCancel(dialog: DialogInterface) {
        listener?.onCancelPressed()
        super.onCancel(dialog)
    }

    interface OnDialogListener {
        fun onAcceptPressed()
        fun onCancelPressed()
    }

    companion object {
        @JvmStatic
        fun newInstance(imageDrawable: Int, message: String) =
            PermissionDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ICON_DRAWABLE, imageDrawable)
                    putString(MESSAGE, message)
                }
            }
    }
}
