package com.naposystems.napoleonchat.ui.changeParams

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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ChangeFakeParamsDialogFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FieldsValidator
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileShareViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.userProfile.UserProfileShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject

private const val CONTACT_ID = "contactId"
private const val CONTACT_NICK = "contactNick"
private const val STATE_NOTIFICATION = "stateNotification"

class ChangeFakeParamsDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(contactId: Int, contactNick: String, stateNotification: Boolean) =
            ChangeFakeParamsDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(CONTACT_ID, contactId)
                    putString(CONTACT_NICK, contactNick)
                    putBoolean(STATE_NOTIFICATION, stateNotification)
                }
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ChangeParamsDialogViewModel by viewModels {
        viewModelFactory
    }
    private val userProfileShareViewModel: UserProfileShareViewModel by viewModels {
        viewModelFactory
    }
    private val contactProfileShareViewModel: ContactProfileShareViewModel by activityViewModels {
        viewModelFactory
    }
    private lateinit var binding: ChangeFakeParamsDialogFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.change_fake_params_dialog_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.buttonAccept.setOnClickListener {
            arguments?.let { args ->
                viewModel.updateNicknameFakeContact(
                    args.getInt(CONTACT_ID), binding.editTextDisplay.text.toString().toLowerCase(
                        Locale.getDefault()
                    )
                )

                if (args.getBoolean(STATE_NOTIFICATION)) {
                    Utils.updateNickNameChannel(
                        requireContext(),
                        args.getInt(CONTACT_ID),
                        args.getString(CONTACT_NICK, ""),
                        binding.editTextDisplay.text.toString().toLowerCase(
                            Locale.getDefault()
                        )
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
        activity?.let { activity ->
            contactProfileShareViewModel.contact.observe(activity, Observer { contact ->
                if (contact != null) {
                    binding.editTextDisplay.setText(contact.getNickName())
                }
            })
        }
    }

    private fun listenerEditText(): TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = Unit

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.buttonAccept.isEnabled =
                FieldsValidator.isNicknameValid(binding.textInputLayoutDisplay)
        }
    }

}