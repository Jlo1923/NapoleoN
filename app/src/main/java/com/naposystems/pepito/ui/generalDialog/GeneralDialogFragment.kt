package com.naposystems.pepito.ui.generalDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.GeneralDialogFragmentBinding

class GeneralDialogFragment : DialogFragment() {

    private lateinit var listener: OnGeneralDialog
    private lateinit var binding: GeneralDialogFragmentBinding
    private lateinit var title: String
    private lateinit var message: String
    private var optionIsCancelable: Boolean = true

    companion object {

        private const val TITLE: String = "TITLE"
        private const val MESSAGE: String = "MESSAGE"
        private const val IS_CANCELABLE: String = "DIALOGTYPE"

        fun newInstance(title: String, message: String, isCancelable: Boolean = true) = GeneralDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TITLE, title)
                putString(MESSAGE, message)
                putBoolean(IS_CANCELABLE, isCancelable)
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
            title = it.getString(TITLE)!!
            message = it.getString(MESSAGE)!!
            optionIsCancelable = it.getBoolean(IS_CANCELABLE)
        }

        binding = DataBindingUtil.inflate(
            inflater, R.layout.general_dialog_fragment, container, false
        )

        binding.textViewTitle.text = title
        binding.textViewMessage.text = message

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
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    fun setListener(listener: OnGeneralDialog) {
        this.listener = listener
    }

}
