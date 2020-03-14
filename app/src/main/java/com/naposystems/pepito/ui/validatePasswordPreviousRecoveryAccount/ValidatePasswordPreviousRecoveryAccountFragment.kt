package com.naposystems.pepito.ui.validatePasswordPreviousRecoveryAccount

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ValidatePasswordPreviousRecoveryAccountFragmentBinding
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
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

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
            }
        })

        binding.buttonRecoveryAccount.setOnClickListener {
            binding.viewSwitcherProgressRecovery.showNext()
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
                    "Title!!",
                    "Error en los datos!!",
                    false,
                    childFragmentManager
                ) {
                    findNavController().popBackStack()
                }
            }
        })

        viewModel.recoveryOlderPasswordCreatingError.observe(viewLifecycleOwner, Observer {
            binding.viewSwitcherProgressRecovery.showNext()
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })
    }

}
