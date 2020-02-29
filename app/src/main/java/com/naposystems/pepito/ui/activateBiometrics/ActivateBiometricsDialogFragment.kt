package com.naposystems.pepito.ui.activateBiometrics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ActivateBiometricsDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class ActivateBiometricsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = ActivateBiometricsDialogFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: ActivateBiometricsViewModel
    private lateinit var binding: ActivateBiometricsDialogFragmentBinding

    private var optionBiometrics: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.activate_biometrics_dialog_fragment, container, false
        )

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            val biometricManager = BiometricManager.from(context!!)

            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    optionBiometrics = when (checkedId) {
                        R.id.radioButton_unlock_fingerprint ->
                            Constants.Biometrics.UNLOCK_WITH_FINGERPRINT.option
                        R.id.radioButton_unlock_faceid ->
                            Constants.Biometrics.UNLOCK_WITH_FACEID.option
                        else -> Constants.Biometrics.WITHOUT_BIOMETRICS.option
                    }
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                    Timber.e("No biometric features available on this device.")
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                    Timber.e("Biometric features are currently unavailable.")
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    Toast.makeText(
                        context,"El dispositivo no tiene asignado un desbloqueo biometrico", Toast.LENGTH_LONG).show()
                }
            }
            viewModel.setBiometricsOption(optionBiometrics)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ActivateBiometricsViewModel::class.java)

        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation

        viewModel.getBiometricsOption()
        viewModel.biometricsOption.observe(viewLifecycleOwner, Observer {
            optionBiometrics = it
            when (it) {
                Constants.Biometrics.UNLOCK_WITH_FINGERPRINT.option ->
                    binding.radioButtonUnlockFingerprint.isChecked = true
                Constants.Biometrics.UNLOCK_WITH_FACEID.option ->
                    binding.radioButtonUnlockFaceid.isChecked = true
                else -> binding.radioButtonWithoutBiometrics.isChecked = true
            }
        })
    }

}