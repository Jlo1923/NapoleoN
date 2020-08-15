package com.naposystems.napoleonchat.ui.generalDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.GeneralDialogFragmentBinding

class GeneralDialogFragment : DialogFragment() {

    private lateinit var listener: OnGeneralDialog
    private lateinit var binding: GeneralDialogFragmentBinding
    private lateinit var title: String
    private lateinit var message: String
    private lateinit var textButtonCancel: String
    private lateinit var textButtonAccept: String
    private var optionIsCancelable: Boolean = true

    companion object {

        private const val TITLE: String = "TITLE"
        private const val MESSAGE: String = "MESSAGE"
        private const val IS_CANCELABLE: String = "DIALOGTYPE"
        private const val TEXT_BUTTON_CANCEL: String = "TEXT_BUTTON_CANCEL"
        private const val TEXT_BUTTON_ACCEPT: String = "TEXT_BUTTON_ACCEPT"

        fun newInstance(
            title: String,
            message: String,
            isCancelable: Boolean = true,
            textButtonAccept: String,
            textButtonCancel: String
        ) = GeneralDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TITLE, title)
                putString(MESSAGE, message)
                putBoolean(IS_CANCELABLE, isCancelable)
                putString(TEXT_BUTTON_CANCEL, textButtonCancel)
                putString(TEXT_BUTTON_ACCEPT, textButtonAccept)
            }
        }
    }

    interface OnGeneralDialog {
        fun onAccept()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            title = it.getString(TITLE, "")
            message = it.getString(MESSAGE, "")
            optionIsCancelable = it.getBoolean(IS_CANCELABLE)
            textButtonAccept = it.getString(TEXT_BUTTON_ACCEPT, "")
            textButtonCancel = it.getString(TEXT_BUTTON_CANCEL, "")
        }

        binding = DataBindingUtil.inflate(
            inflater, R.layout.general_dialog_fragment, container, false
        )

        binding.textViewTitle.text = title
        binding.textViewMessage.text = message

        if (textButtonCancel.isNotEmpty()) {
            binding.buttonCancel.text = textButtonCancel
        }

        if (textButtonAccept.isNotEmpty()) {
            binding.buttonAccept.text = textButtonAccept
        }

        isCancelable = optionIsCancelable

        binding.buttonCancel.isVisible = optionIsCancelable

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            listener.onAccept()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let {
            it.attributes.windowAnimations = R.style.DialogAnimation
            it.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun setListener(listener: OnGeneralDialog) {
        this.listener = listener
    }

}
