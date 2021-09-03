package com.naposystems.napoleonchat.ui.securitySettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SecuritySettingsFragmentBinding
import com.naposystems.napoleonchat.dialog.activateBiometrics.ActivateBiometricsDialogFragment
import com.naposystems.napoleonchat.dialog.selfDestructTime.Location
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogFragment
import com.naposystems.napoleonchat.dialog.selfDestructTime.SelfDestructTimeDialogViewModel
import com.naposystems.napoleonchat.dialog.timeAccessPin.TimeAccessPinDialogFragment
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment.SelfDestructTimeMessageNotSentDialogFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener
import com.naposystems.napoleonchat.utility.showCaseManager.ShowCaseManager
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import javax.inject.Inject

class SecuritySettingsFragment : BaseFragment() {

    companion object {
        fun newInstance() = SecuritySettingsFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SecuritySettingsViewModel
    private lateinit var binding: SecuritySettingsFragmentBinding
    private var showCase: ShowCaseManager? = null

    private val selfDestructTimeViewModel: SelfDestructTimeDialogViewModel by viewModels {
        viewModelFactory
    }

    private val args: SecuritySettingsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.security_settings_fragment, container, false
        )

        binding.lifecycleOwner = this

        //region CONVERSATION
        binding.optionMessageSelfDestruct.setSafeOnClickListener { optionMessageClickListener() }
        binding.imageButtonMessageOptionEndIcon.setSafeOnClickListener { optionMessageClickListener() }

        /*binding.optionAllowDownload.setOnClickListener {
            binding.switchAllowDownload.isChecked = !binding.switchAllowDownload.isChecked
        }
        binding.switchAllowDownload.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAllowDownload(isChecked)
        }*/

        binding.optionMessageSelfDestructTimeNotSent.setSafeOnClickListener {
            optionMessageSelfDestructTimeNotSentClickListener()
        }
        binding.imageButtonMessageNotSendOptionEndIcon.setSafeOnClickListener {
            optionMessageSelfDestructTimeNotSentClickListener()
        }
        //endregion

        //region SECURITY
        binding.optionEditAccessPin.setSafeOnClickListener { optionEditAccessPinClickListener() }
        binding.imageButtonEditAccessPinOptionEndIcon.setSafeOnClickListener {
            optionEditAccessPinClickListener()
        }

        binding.optionBiometrics.setSafeOnClickListener { optionBiometrictsClickListener() }
        binding.imageButtonBiometricsEndIcon.setSafeOnClickListener { optionBiometrictsClickListener() }

        binding.optionTimeRequestAccessPin.setSafeOnClickListener { optionTimeAccessPinClickListener() }
        binding.imageButtonTimeOptionEndIcon.setSafeOnClickListener { optionTimeAccessPinClickListener() }
        //endregion

        binding.optionAccountRecoveryInformation.setSafeOnClickListener {
            optionRegisterRecoveryAccountClickListener()
        }
        binding.imageButtonAccountRecoveryOptionEndIcon.setSafeOnClickListener {
            optionRegisterRecoveryAccountClickListener()
        }

        return binding.root
    }

    override fun onPause() {
        showCase?.setPaused(true)
        showCase?.dismiss()
        super.onPause()
    }

    override fun onResume() {
        showCase?.setPaused(false)
        if (args.showShowCase) {
            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
            showCase = ShowCaseManager().apply {
                setActivity(requireActivity())
                setListener(object : ShowCaseManager.Listener {
                    override fun openSecuritySettings() = Unit
                })
                setSixthView(binding.imageViewAccountRecoveryOptionStartIcon)
                showSixth { findNavController().popBackStack() }
            }
        }
        super.onResume()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(SecuritySettingsViewModel::class.java)

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

        viewModel.allowDownloads.observe(viewLifecycleOwner) {
            binding.allowDownloads = it
            binding.executePendingBindings()
        }

        viewModel.timeRequestAccessPin.observe(viewLifecycleOwner) {
            binding.timeRequestAccessPin = it
            binding.executePendingBindings()
        }

        selfDestructTimeViewModel.selfDestructTimeGlobal.observe(viewLifecycleOwner) {
            binding.selfDestructTimeGlobal = it
            binding.executePendingBindings()
        }

        selfDestructTimeViewModel.messageSelfDestructTimeNotSent.observe(viewLifecycleOwner) {
            binding.messageSelfDestructTimeNotSent = it
            binding.executePendingBindings()
        }

    }

    private fun optionMessageClickListener() {
        val dialog = SelfDestructTimeDialogFragment.newInstance(0, Location.SECURITY_SETTINGS)
        dialog.setListener(object : SelfDestructTimeDialogFragment.SelfDestructTimeListener {
            override fun onSelfDestructTimeChange(selfDestructTimeSelected: Int) {
                selfDestructTimeViewModel.getSelfDestructTime()
            }
        })
        dialog.show(childFragmentManager, "SelfDestructTime")
    }

    private fun optionEditAccessPinClickListener() {
        findNavController().currentDestination?.getAction(R.id.action_securitySettingsFragment_to_editAccessPinFragment)
            ?.let {
                this.findNavController().navigate(
                    SecuritySettingsFragmentDirections
                        .actionSecuritySettingsFragmentToEditAccessPinFragment()
                )
            }
    }

    private fun optionBiometrictsClickListener() {
        val dialog = ActivateBiometricsDialogFragment()
        dialog.show(childFragmentManager, "BiometricsSelection")
    }

    private fun optionTimeAccessPinClickListener() {
        val dialog = TimeAccessPinDialogFragment()
        dialog.setListener(object : TimeAccessPinDialogFragment.TimeAccessPinListener {
            override fun onTimeAccessChange() {
                viewModel.getTimeRequestAccessPin()
            }
        })
        dialog.show(childFragmentManager, "TimeRequestAccessPin")
    }

    private fun optionMessageSelfDestructTimeNotSentClickListener() {
        val dialog = SelfDestructTimeMessageNotSentDialogFragment()
        dialog.setListener(
            object :
                SelfDestructTimeMessageNotSentDialogFragment.MessageSelfDestructTimeNotSentListener {
                override fun onDestructMessageChange() {
                    selfDestructTimeViewModel.getMessageSelfDestructTimeNotSent()
                }
            })
        dialog.show(childFragmentManager, "MessageSelfDestructTimeNotSent")
    }

    private fun optionRegisterRecoveryAccountClickListener() {
        findNavController().currentDestination?.getAction(R.id.action_securitySettingsFragment_to_registerRecoveryAccountFragment)
            ?.let {
                findNavController().navigate(
                    SecuritySettingsFragmentDirections
                        .actionSecuritySettingsFragmentToRegisterRecoveryAccountFragment()
                )
            }
    }
}
