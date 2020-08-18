package com.naposystems.napoleonchat.ui.recoveryAccount

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.RecoveryAccountFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.Utils.Companion.generalDialog
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RecoveryAccountFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryAccountFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: RecoveryAccountFragmentBinding
    private val viewModel: RecoveryAccountViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var snackbarUtils: SnackbarUtils
    private var successToken: Boolean = false

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
            R.layout.recovery_account_fragment, container, false
        )

        setupCallbackFirebase()

        binding.textInputEditTextNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonRecoveryAccount.isEnabled = s!!.length >= 4 && successToken
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.buttonRecoveryAccount.setOnClickListener {
            Utils.hideKeyboard(binding.textInputEditTextNickname)
            viewModel.sendNickname(binding.textInputEditTextNickname.text.toString().toLowerCase())
            binding.viewSwitcherRecoveryAccount.showNext()
        }

        return binding.root
    }

    private fun setupCallbackFirebase() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    context?.let { context ->
                        this.showToast(context.getString(R.string.text_fail))
                    }
                    return@OnCompleteListener
                }

                task.result?.token?.let { token ->
                    viewModel.setFirebaseId(token)
                }
            })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userType.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it.userType == Constants.UserType.NEW_USER.type) {

                    findNavController().navigate(
                        RecoveryAccountFragmentDirections
                            .actionRecoveryAccountFragmentToRecoveryAccountQuestionsFragment(
                                it,
                                binding.textInputEditTextNickname.text.toString()
                            )
                    )

                } else if (it.userType == Constants.UserType.OLD_USER.type) {
                    findNavController().navigate(
                        RecoveryAccountFragmentDirections
                            .actionRecoveryAccountFragmentToValidatePasswordPreviousRecoveryAccountFragment(
                                binding.textInputEditTextNickname.text.toString()
                            )
                    )
                }
                viewModel.resetRecoveryQuestions()
            }
        })

        viewModel.recoveryErrorForAttempts.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                generalDialog(
                    getString(R.string.text_title_block_attempts),
                    getString(R.string.text_description_block_attempts),
                    false,
                    childFragmentManager
                ) {
                    findNavController().popBackStack()
                }
            }
        })

        viewModel.recoveryQuestionsCreatingError.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
            binding.viewSwitcherRecoveryAccount.showPrevious()
        })

        viewModel.successToken.observe(viewLifecycleOwner, Observer {
            this.successToken = it
        })
    }
}
