package com.naposystems.napoleonchat.ui.validatePasswordPreviousRecoveryAccount

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
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.ValidatePasswordPreviousRecoveryAccountFragmentBinding
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils.Companion.generalDialog
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ValidatePasswordPreviousRecoveryAccountFragment : Fragment() {

    companion object {
        fun newInstance() = ValidatePasswordPreviousRecoveryAccountFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: ValidatePasswordPreviousRecoveryAccountViewModel by viewModels{
        viewModelFactory
    }

    private lateinit var binding: ValidatePasswordPreviousRecoveryAccountFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils

    private val args: ValidatePasswordPreviousRecoveryAccountFragmentArgs by navArgs()
    private lateinit var nickname: String

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
            R.layout.validate_password_previous_recovery_account_fragment,
            container,
            false
        )

        nickname = args.nickname

        binding.textInputEditTextPassword.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                binding.buttonRecoveryAccount.isEnabled = s!!.length >= 4
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })

        binding.buttonRecoveryAccount.setOnClickListener {
            binding.viewSwitcherRecoveryAccount.showNext()
            viewModel.sendPassword(nickname, binding.textInputEditTextPassword.text.toString())
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.passwordSuccess.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(
                    ValidatePasswordPreviousRecoveryAccountFragmentDirections
                        .actionValidatePasswordPreviousRecoveryAccountFragmentToRecoveryOlderAccountQuestionsFragment(
                            nickname
                        )
                )
            } else {
                viewModel.setAttemptPref()
                generalDialog(
                    getString(R.string.text_alert_failure),
                    getString(R.string.text_password_incorrect),
                    false,
                    childFragmentManager
                ) {
                    findNavController().popBackStack()
                }
            }
        })

        viewModel.recoveryOlderPasswordCreatingError.observe(viewLifecycleOwner, Observer {
            binding.viewSwitcherRecoveryAccount.showNext()
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })
    }

}
