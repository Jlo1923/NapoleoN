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
import com.naposystems.napoleonchat.source.remote.dto.user.DisplayNameReqDTO
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FieldsValidator
import com.naposystems.napoleonchat.utility.SnackbarUtils
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
    ): View {
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
                    else -> {
                        userProfileShareViewModel.userEntity.value?.let { user ->
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
        viewModel.changeParamsWsError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })

        observers()

    }

    private fun observers() {
        when (arguments?.getInt(OPTION)) {
            Constants.ChangeParams.NAME_USER.option -> {
                userProfileShareViewModel.userUpdated.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        dismiss()
                    }
                })
                userProfileShareViewModel.userEntity.observe(viewLifecycleOwner, Observer { user ->
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
                            binding.editTextDisplay.setText(contact.getName())
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
            binding.buttonAccept.isEnabled =
                FieldsValidator.isDisplayNameValid(binding.textInputLayoutDisplay)
        }
    }

}
