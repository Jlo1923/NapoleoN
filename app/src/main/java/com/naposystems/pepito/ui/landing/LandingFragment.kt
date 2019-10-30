package com.naposystems.pepito.ui.landing

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.LandingFragmentBinding

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

        binding.buttonSeleccionIdioma.setOnClickListener {
            viewModel.onShowLanguageSelectionPressed()
        }

        binding.buttonRegister.setOnClickListener {
            viewModel.onRegisterButtonPressed()
        }

        viewModel.showLanguageSelection.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onLanguageSelectionShowed()
                this.findNavController()
                    .navigate(LandingFragmentDirections.actionLandingFragmentToLanguageSelectionDialog())
            }
        })

        viewModel.openRegister.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                viewModel.onRegisterOpened()
                this.findNavController()
                    .navigate(LandingFragmentDirections.actionLandingFragmentToRegisterFragment())
            }
        })

        return binding.root
    }

}
