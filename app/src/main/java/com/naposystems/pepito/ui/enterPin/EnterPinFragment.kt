package com.naposystems.pepito.ui.enterPin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EnterPinFragmentBinding
import com.naposystems.pepito.ui.custom.EnterCodeWidget
import com.naposystems.pepito.ui.custom.numericKeyboard.NumericKeyboardCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class EnterPinFragment : Fragment(),
    EnterCodeWidget.OnEventListener,
    NumericKeyboardCustomView.OnEventListener {

    companion object {
        fun newInstance() = EnterPinFragment()
        const val MAX_ATTEMPTS = 3
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: EnterPinViewModel by viewModels { viewModelFactory }
    private lateinit var binding: EnterPinFragmentBinding

    private var biometricsOption = 0
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.enter_pin_fragment, container, false
        )

        binding.enterCodeWidget.setListener(this)
        binding.numericKeyboard.setListener(this)

        validateBiometrics()

        binding.imageButtonFingerprint.setOnClickListener {
            showBiometricPrompt()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getBiometricsOption()
        viewModel.biometricsOption.observe(viewLifecycleOwner, Observer {
            biometricsOption = it
        })

        viewModel.validPassword.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(
                    EnterPinFragmentDirections.actionEnterPinFragmentToHomeFragment()
                )
                Utils.hideKeyboard(binding.container)
            } else {
                binding.enterCodeWidget.showError()
            }
        })
        viewModel.getAttempts()
        viewModel.attempts.observe(viewLifecycleOwner, Observer {
            if (it == MAX_ATTEMPTS) {
                findNavController().navigate(
                    EnterPinFragmentDirections.actionEnterPinFragmentToUnlockAppTimeFragment()
                )
            } else {

                binding.textViewAttempts.apply {
                    text = getString(R.string.text_number_attempts, it, MAX_ATTEMPTS)
                }
            }
        })
    }

    override fun onImeActionDone() {
        viewModel.validatedAccessPin(binding.enterCodeWidget.getCode())
    }

    override fun onCodeCompleted(isCompleted: Boolean) {
        if (isCompleted) {
            viewModel.validatedAccessPin(binding.enterCodeWidget.getCode())
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(context)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.setAttempts(0)
                    viewModel.setTotalAttempts(0)
                    viewModel.setLockStatus(Constants.LockStatus.UNLOCK.state)

                    findNavController().navigate(
                        EnterPinFragmentDirections.actionEnterPinFragmentToHomeFragment()
                    )
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.text_title_dialog_biometrics))
            .setDescription(getString(R.string.text_description_dialog_biometrics))
            .setConfirmationRequired(false)
            .setNegativeButtonText(getText(R.string.text_cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun validateBiometrics() {
        val biometricManager = BiometricManager.from(requireContext())

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                if (biometricsOption == Constants.Biometrics.UNLOCK_WITH_FINGERPRINT.option) {
                    showBiometricPrompt()
                    binding.imageButtonFingerprint.visibility = View.VISIBLE
                } else {
                    binding.imageButtonFingerprint.visibility = View.GONE
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                binding.imageButtonFingerprint.visibility = View.GONE
                viewModel.setBiometricPreference(Constants.Biometrics.WITHOUT_BIOMETRICS.option)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        validateBiometrics()
    }

    override fun onKeyPressed(keyCode: Int) {
        binding.enterCodeWidget.setAddNumber(keyCode)
        binding.numericKeyboard.showDeleteKey(binding.enterCodeWidget.getCode().isNotEmpty())
    }

    override fun onDeletePressed() {
        binding.enterCodeWidget.deleteNumber()
        binding.numericKeyboard.showDeleteKey(binding.enterCodeWidget.getCode().isNotEmpty())
    }
}
