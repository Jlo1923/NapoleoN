package com.naposystems.pepito.ui.generalDialog

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.GeneralDialogFragmentBinding

class GeneralDialogFragment : DialogFragment() {

    private lateinit var listener: OnGeneralDialog
    private lateinit var binding: GeneralDialogFragmentBinding
    private lateinit var title: String
    private lateinit var message: String

    companion object {

        private const val TITLE: String = "TITLE"
        private const val MESSAGE: String = "MESSAGE"

        fun newInstance(title: String, message: String) = GeneralDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TITLE, title)
                putString(MESSAGE, message)
            }
        }
    }

    interface OnGeneralDialog {
        fun onAccept()
    }

    private lateinit var viewModel: GeneralDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        arguments?.let {
            title = it.getString(TITLE)!!
            message = it.getString(MESSAGE)!!
        }

        binding = DataBindingUtil.inflate(
            inflater, R.layout.general_dialog_fragment, container, false
        )

        binding.textViewTitle.text = title
        binding.textViewMessage.text = message

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
        viewModel = ViewModelProviders.of(this)
            .get(GeneralDialogViewModel::class.java)

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
