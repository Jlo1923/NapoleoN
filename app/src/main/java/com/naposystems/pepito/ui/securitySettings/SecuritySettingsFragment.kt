package com.naposystems.pepito.ui.securitySettings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SecuritySettingsFragmentBinding
import com.naposystems.pepito.ui.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.pepito.ui.activateBiometrics.ActivateBiometricsViewModel
import com.naposystems.pepito.ui.selfDestructTime.Location
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.pepito.ui.selfDestructTime.SelfDestructTimeViewModel
import com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogFragment
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

    private val selfDestructTimeViewModel: SelfDestructTimeViewModel by viewModels {
        viewModelFactory
    }

    private val activateBiometricsViewModel: ActivateBiometricsViewModel by viewModels {
        viewModelFactory
    }

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

        //region CONVERSATION
        binding.optionMessageSelfDestruct.setOnClickListener(optionMessageClickListener())
        binding.imageButtonMessageOptionEndIcon.setOnClickListener(optionMessageClickListener())

        binding.optionAllowDownload.setOnClickListener {
            binding.switchAllowDownload.isChecked = !binding.switchAllowDownload.isChecked
        }
        binding.switchAllowDownload.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAllowDownload(isChecked)
        }

        binding.optionMessageSelfDestructTimeNotSent.setOnClickListener(
            optionMessageSelfDestructTimeNotSentClickListener()
        )
        binding.imageButtonMessageNotSendOptionEndIcon.setOnClickListener(
            optionMessageSelfDestructTimeNotSentClickListener()
        )
        //endregion

        //region SECURITY
        binding.optionEditAccessPin.setOnClickListener(optionEditAccessPinClickListener())
        binding.imageButtonEditAccessPinOptionEndIcon.setOnClickListener(
            optionEditAccessPinClickListener()
        )

        binding.optionBiometrics.setOnClickListener{optionBiometrictsClickListener()}
        binding.imageButtonBiometricsEndIcon.setOnClickListener{optionBiometrictsClickListener()}

        binding.optionTimeRequestAccessPin.setOnClickListener(optionTimeAccessPinClickListener())
        binding.imageButtonTimeOptionEndIcon.setOnClickListener(optionTimeAccessPinClickListener())
        //endregion

        binding.optionAccountRecoveryInformation.setOnClickListener(
            optionRegisterRecoveryAccountClickListener()
        )
        binding.imageButtonAccountRecoveryOptionEndIcon.setOnClickListener(
            optionRegisterRecoveryAccountClickListener()
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SecuritySettingsViewModel::class.java)

        binding.viewModel = viewModel
        binding.selfDestructViewModel = selfDestructTimeViewModel


        selfDestructTimeViewModel.getSelfDestructTime()
        selfDestructTimeViewModel.getMessageSelfDestructTimeNotSent()
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
        val dialog = SelfDestructTimeDialogFragment.newInstance(0, Location.SECURITY_SETTINGS)
        dialog.setListener(object : SelfDestructTimeDialogFragment.SelfDestructTimeListener {
            override fun onSelfDestructTimeChange(selfDestructTimeSelected: Int) {
                selfDestructTimeViewModel.getSelfDestructTime()
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

    private fun optionBiometrictsClickListener() {
        val dialog = ActivateBiometricsDialogFragment()
        dialog.show(childFragmentManager, "BiometricsSelection")
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

    private fun optionMessageSelfDestructTimeNotSentClickListener() = View.OnClickListener {
        val dialog = SelfDestructTimeMessageNotSentDialogFragment()
        dialog.setListener(
            object : SelfDestructTimeMessageNotSentDialogFragment.MessageSelfDestructTimeNotSentListener {
                override fun onDestructMessageChange() {
                    selfDestructTimeViewModel.getMessageSelfDestructTimeNotSent()
                }
            })
        dialog.show(childFragmentManager, "MessageSelfDestructTimeNotSent")
    }

    private fun optionRegisterRecoveryAccountClickListener() = View.OnClickListener {
        findNavController().navigate(
            SecuritySettingsFragmentDirections
                .actionSecuritySettingsFragmentToRegisterRecoveryAccountFragment()
        )
    }
}
