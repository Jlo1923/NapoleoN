package com.naposystems.pepito.ui.recoveryAccount

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
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RecoveryAccountFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
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

        binding.textInputEditTextNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonRecoveryAccount.isEnabled = s!!.length >= 5
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
            }
        })

        binding.buttonRecoveryAccount.setOnClickListener {
            Utils.hideKeyboard(binding.textInputEditTextNickname)
            viewModel.sendNickname(binding.textInputEditTextNickname.text.toString())
            binding.viewSwitcherRecoveryAccount.showNext()
        }

        return binding.root
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
                    "Intentos Agotados|!!",
                    "Se le ha bloqueado por cantidad de intentos|!!",
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
    }
}
