package com.naposystems.pepito.ui.changeFakes

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ChangeFakesDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

private const val CONTACT_ID = "contactId"
private const val OPTION = "option"

class ChangeFakesDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(contactId : Int , option : Int) = ChangeFakesDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(CONTACT_ID, contactId)
                putInt(OPTION, option)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ChangeFakesDialogViewModel
    private val contactProfileShareViewModel : ContactProfileShareViewModel by activityViewModels {
        viewModelFactory
    }
    private lateinit var binding: ChangeFakesDialogFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.change_fakes_dialog_fragment, container, false)

        binding.lifecycleOwner = this

        binding.buttonAccept.setOnClickListener {
            arguments?.let { args ->
                if(args.getInt(OPTION) == Constants.ChangeFake.NAME.option) {
                    viewModel.updateNameFakeContact(
                        args.getInt(CONTACT_ID), binding.editTextDisplay.text.toString()
                    )
                }
                else {
                    viewModel.updateNicknameFakeContact(
                        args.getInt(CONTACT_ID), binding.editTextDisplay.text.toString()
                    )
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.editTextDisplay.addTextChangedListener(listenerEditText())

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.let { dialog ->
            dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
            dialog.window?.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ChangeFakesDialogViewModel::class.java)

        viewModel.responseEditFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                dismiss()
            }
        })

        activity?.let {activity ->
            contactProfileShareViewModel.contact.observe(activity, Observer { contact ->
                if (contact != null) {
                    when(arguments?.getInt(OPTION)) {
                        Constants.ChangeFake.NAME.option -> {
                            binding.editTextDisplay.setText(contact.getName())
                        }else -> {
                        binding.editTextDisplay.setText(contact.getNickName())
                    }
                    }
                }
            })
        }

        setupTitle()
    }

    private fun listenerEditText(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            if(count == 0) {
                when(arguments?.getInt(OPTION)) {
                    Constants.ChangeFake.NAME.option -> {
                        enabledButtonAccept(true)
                    }
                    else -> {
                        enabledButtonAccept(false)
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (count) {
                in 1..4 -> {
                    enabledButtonAccept(false)
                }
                0 -> {
                    if(arguments?.getInt(OPTION) == Constants.ChangeFake.NAME.option) {
                        enabledButtonAccept(true)
                    } else {
                        enabledButtonAccept(false)
                    }
                }
                else -> {
                    enabledButtonAccept(true)
                }
            }
        }
    }

    private fun enabledButtonAccept(boolean : Boolean) {
        if(isResumed) {
            binding.buttonAccept.isEnabled = boolean
            if(boolean){
                binding.textInputLayoutDisplay.error = null
            } else {
                when(arguments?.getInt(OPTION)) {
                    Constants.ChangeFake.NAME.option -> {
                        binding.textInputLayoutDisplay.error = getString(R.string.text_name_not_contain_enough_char)
                    }
                    else -> {
                        binding.textInputLayoutDisplay.error = getString(R.string.text_nickname_not_contain_enough_char)
                    }
                }
            }
        }
    }

    private fun setupTitle() {
        val string =when(arguments?.get(OPTION)) {
            Constants.ChangeFake.NAME.option -> {
                R.string.text_name
            }
            else -> {
                R.string.text_nickname
            }
        }
        binding.textViewTitle.text = context?.getString(string)
    }

}
