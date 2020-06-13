package com.naposystems.pepito.ui.changeParams

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ChangeFakesDialogFragmentBinding
import com.naposystems.pepito.dto.user.DisplayNameReqDTO
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.pepito.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

private const val CONTACT_ID = "contactId"
private const val OPTION = "option"

class ChangeParamsDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(contactId: Int, option: Int) = ChangeParamsDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(CONTACT_ID, contactId)
                putInt(OPTION, option)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ChangeParamsDialogViewModel by viewModels { viewModelFactory }
    private val contactProfileShareViewModel: ContactProfileShareViewModel by activityViewModels {
        viewModelFactory
    }
    private val userProfileShareViewModel: UserProfileShareViewModel by viewModels {
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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.change_fakes_dialog_fragment,
            container,
            false
        )

        binding.lifecycleOwner = this

        binding.buttonAccept.setOnClickListener {
            arguments?.let { args ->
                when (args.getInt(OPTION)) {
                    Constants.ChangeParams.NAME_FAKE.option -> {
                        viewModel.updateNameFakeContact(
                            args.getInt(CONTACT_ID), binding.editTextDisplay.text.toString()
                        )
                    }
                    Constants.ChangeParams.NICKNAME_FAKE.option -> {
                        viewModel.updateNicknameFakeContact(
                            args.getInt(CONTACT_ID), binding.editTextDisplay.text.toString()
                        )
                    }
                    else -> {
                        userProfileShareViewModel.user.value?.let { user ->
                            userProfileShareViewModel.updateUserInfo(
                                user,
                                DisplayNameReqDTO(
                                    displayName = binding.editTextDisplay.text.toString()
                                )
                            )
                        }
                    }
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

        userProfileShareViewModel.getUser()

        viewModel.responseEditFake.observe(viewLifecycleOwner, Observer {
            if (it) {
                dismiss()
            }
        })

        observers()

        setupTitle()
    }

    private fun observers() {
        when (arguments?.getInt(OPTION)) {
            Constants.ChangeParams.NAME_USER.option -> {
                userProfileShareViewModel.userUpdated.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        dismiss()
                    }
                })
                userProfileShareViewModel.user.observe(viewLifecycleOwner, Observer { user ->
                    if (user != null) {
                        binding.editTextDisplay.setText(user.displayName)
                    }
                })
                userProfileShareViewModel.errorUpdatingUser.observe(viewLifecycleOwner, Observer {
                    if (it.isNotEmpty()) {
                        Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
                    }
                })
            }
            else -> {
                activity?.let { activity ->
                    contactProfileShareViewModel.contact.observe(activity, Observer { contact ->
                        if (contact != null) {
                            when (arguments?.getInt(OPTION)) {
                                Constants.ChangeParams.NAME_FAKE.option -> {
                                    binding.editTextDisplay.setText(contact.getName())
                                }
                                Constants.ChangeParams.NICKNAME_FAKE.option -> {
                                    binding.editTextDisplay.setText(contact.getNickName())
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun listenerEditText(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when  {
                binding.editTextDisplay.text?.count() in 1..4 &&
                        arguments?.getInt(OPTION) == Constants.ChangeParams.NICKNAME_FAKE.option ->
                    enabledButtonAccept(false)
                binding.editTextDisplay.text?.count() in 1..1 && (
                        arguments?.getInt(OPTION) == Constants.ChangeParams.NAME_FAKE.option ||
                                arguments?.getInt(OPTION) == Constants.ChangeParams.NAME_USER.option) ->
                    enabledButtonAccept(false)
                binding.editTextDisplay.text?.count() == 0 -> enabledButtonAccept(true)
                else -> enabledButtonAccept(true)
            }
        }
    }

    private fun enabledButtonAccept(boolean: Boolean) {
        if (isResumed) {
            binding.buttonAccept.isEnabled = boolean
            if (boolean) {
                binding.textInputLayoutDisplay.error = null
            } else {
                when (arguments?.getInt(OPTION)) {
                    Constants.ChangeParams.NAME_FAKE.option,
                    Constants.ChangeParams.NAME_USER.option -> {
                        binding.textInputLayoutDisplay.error =
                            getString(R.string.text_name_not_contain_enough_char)
                    }
                    else -> {
                        binding.textInputLayoutDisplay.error =
                            getString(R.string.text_nickname_not_contain_enough_char)
                    }
                }
            }
        }
    }

    private fun setupTitle() {
        val string = when (arguments?.get(OPTION)) {
            Constants.ChangeParams.NICKNAME_FAKE.option -> {
                R.string.text_nickname
            }
            else -> {
                R.string.text_name
            }
        }
        binding.textViewTitle.text = context?.getString(string)
    }

}
