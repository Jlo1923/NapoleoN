package com.naposystems.napoleonchat.ui.landing

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.LandingFragmentBinding
import com.naposystems.napoleonchat.dialog.languageSelection.LanguageSelectionDialogFragment
import com.naposystems.napoleonchat.utility.Constants

class LandingFragment : Fragment() {

    companion object {
        fun newInstance() = LandingFragment()
    }

    private lateinit var viewModel: LandingViewModel
    private lateinit var binding: LandingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LandingViewModel::class.java)

        binding = DataBindingUtil.inflate(inflater, R.layout.landing_fragment, container, false)
        binding.viewModel = viewModel

        viewModel.showLanguageSelection.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onLanguageSelectionShowed()
                val languageSelectionDialog = LanguageSelectionDialogFragment
                    .newInstance(
                        Constants.LocationSelectionLanguage.LANDING.location
                    )
                languageSelectionDialog.show(childFragmentManager, "LanguageSelection")
            }
        })

        viewModel.openSendCode.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onSendCodeOpened()
                this.findNavController()
                    .navigate(LandingFragmentDirections.actionLandingFragmentToSendCodeFragment())
            }
        })

        viewModel.openRecoveryAccount.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onRecoveryAccountOpened()
                this.findNavController()
                    .navigate(LandingFragmentDirections.actionLandingFragmentToRecoveryAccountFragment())
            }
        })

        return binding.root
    }

}
