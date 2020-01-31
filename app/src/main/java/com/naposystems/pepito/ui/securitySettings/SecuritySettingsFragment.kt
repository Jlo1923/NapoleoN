package com.naposystems.pepito.ui.securitySettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SecuritySettingsFragmentBinding
import com.naposystems.pepito.ui.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SecuritySettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SecuritySettingsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SecuritySettingsViewModel
    private lateinit var binding: SecuritySettingsFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.security_settings_fragment, container, false
        )

        binding.lifecycleOwner = this

        //region GENERAL
        binding.optionMessageSelfDestruct.setOnClickListener(optionMessageClickListener())
        binding.imageButtonMessageOptionEndIcon.setOnClickListener(optionMessageClickListener())

        binding.optionAllowDownload.setOnClickListener {
            binding.switchAllowDownload.isChecked = !binding.switchAllowDownload.isChecked
        }
        binding.switchAllowDownload.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAllowDownload(isChecked)
        }

        //endregion

        //region SECURITY
        binding.optionEditAccessPin.setOnClickListener(optionEditAccessPinClickListener())
        binding.imageButtonEditAccessPinOptionEndIcon.setOnClickListener(
            optionEditAccessPinClickListener()
        )

        binding.optionBiometrics.setOnClickListener(optionBiometrictsClickListener())
        binding.imageButtonBiometricsEndIcon.setOnClickListener(optionBiometrictsClickListener())

        binding.optionTimeRequestAccessPin.setOnClickListener(optionTimeAccessPinClickListener())
        binding.imageButtonTimeOptionEndIcon.setOnClickListener(optionTimeAccessPinClickListener())

        binding.optionAccountRecoveryInformation.setOnClickListener(
            optionRegisterRecoveryAccountClickListener()
        )
        binding.imageButtonAccountRecoveryOptionEndIcon.setOnClickListener(
            optionRegisterRecoveryAccountClickListener()
        )

        //endregion

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SecuritySettingsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getSelfDestructTime()
        viewModel.getTimeRequestAccessPin()
        viewModel.getAllowDownload()
        viewModel.getBiometricsOption()

        viewModel.biometricsOption.observe(viewLifecycleOwner, Observer {
            if (it == Constants.Biometrics.BIOMETRICS_NOT_FOUND.option) {
                binding.optionBiometrics.visibility = View.GONE
            }
        })
    }

    private fun optionMessageClickListener() = View.OnClickListener {
        val dialog = SelfDestructTimeDialogFragment()
        dialog.setListener(object : SelfDestructTimeDialogFragment.SelfDestructTimeListener {
            override fun onSelfDestructTimeChange() {
                viewModel.getSelfDestructTime()
            }
        })
        dialog.show(childFragmentManager, "SelfDestructTime")
    }

    private fun optionEditAccessPinClickListener() = View.OnClickListener {
        this.findNavController().navigate(
            SecuritySettingsFragmentDirections
                .actionSecuritySettingsFragmentToEditAccessPinFragment()
        )
    }

    private fun optionBiometrictsClickListener() = View.OnClickListener {
        val biometricsDialog = ActivateBiometricsDialogFragment()
        biometricsDialog.show(childFragmentManager, "BiometricsSelection")
    }

    private fun optionTimeAccessPinClickListener() = View.OnClickListener {
        val dialog = TimeAccessPinDialogFragment()
        dialog.setListener(object : TimeAccessPinDialogFragment.TimeAccessPinListener {
            override fun onTimeAccessChange() {
                viewModel.getTimeRequestAccessPin()
            }
        })
        dialog.show(childFragmentManager, "TimeRequestAccessPin")
    }

    private fun optionRegisterRecoveryAccountClickListener() = View.OnClickListener {
        findNavController().navigate(
            SecuritySettingsFragmentDirections
                .actionSecuritySettingsFragmentToRegisterRecoveryAccountFragment()
        )
    }
}
