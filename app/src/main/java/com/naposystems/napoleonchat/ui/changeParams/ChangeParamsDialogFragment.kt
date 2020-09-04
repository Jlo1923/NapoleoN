package com.naposystems.napoleonchat.ui.changeParams

import android.content.Context
import android.os.Bundle
import android.text.*
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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ChangeFakesDialogFragmentBinding
import com.naposystems.napoleonchat.dto.user.DisplayNameReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FieldsValidator
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
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
                            args.getInt(CONTACT_ID),
                            binding.editTextDisplay.text.toString().trim()
                                .replace("\\s+".toRegex(), " ")
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

        val displayLengthFilter = InputFilter.LengthFilter(
            resources.getInteger(R.integer.max_length_display_name)
        )
        val nicknameLengthFilter = InputFilter.LengthFilter(
            resources.getInteger(R.integer.max_length_nickname)
        )

        when (arguments?.getInt(OPTION)) {
            Constants.ChangeParams.NAME_USER.option,
            Constants.ChangeParams.NAME_FAKE.option -> {
                binding.editTextDisplay.apply {
                    this.inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                    this.filters = arrayOf(displayLengthFilter, object : InputFilter {
                        override fun filter(
                            source: CharSequence,
                            start: Int,
                            end: Int,
                            dest: Spanned,
                            dstart: Int,
                            dend: Int
                        ): CharSequence? {
                            if (source == "") {
                                return source
                            }

                            return if (source.toString().matches("[a-zA-Z ]+".toRegex())) {
                                source
                            } else {
                                null
                            }
                        }
                    })
                }
            }
            Constants.ChangeParams.NICKNAME_FAKE.option -> {
                binding.editTextDisplay.apply {
                    this.inputType = InputType.TYPE_CLASS_TEXT
                    this.filters = arrayOf(nicknameLengthFilter, object : InputFilter {
                        override fun filter(
                            source: CharSequence,
                            start: Int,
                            end: Int,
                            dest: Spanned,
                            dstart: Int,
                            dend: Int
                        ): CharSequence? {
                            if (source == "") {
                                return source
                            }

                            return if (source.toString().matches("[a-zA-Z._0-9]+".toRegex())) {
                                source
                            } else {
                                null
                            }
                        }
                    })
                }
            }
        }

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
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (arguments?.getInt(OPTION)) {
                Constants.ChangeParams.NAME_USER.option,
                Constants.ChangeParams.NAME_FAKE.option -> {
                    binding.buttonAccept.isEnabled =
                        FieldsValidator.isDisplayNameValid(binding.textInputLayoutDisplay)
                }
                Constants.ChangeParams.NICKNAME_FAKE.option -> {
                    binding.buttonAccept.isEnabled =
                        FieldsValidator.isNicknameValid(binding.textInputLayoutDisplay)
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
