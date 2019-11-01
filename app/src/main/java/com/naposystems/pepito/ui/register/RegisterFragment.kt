package com.naposystems.pepito.ui.register

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RegisterFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FieldsValidator

class RegisterFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterFragment()
    }

    private lateinit var viewModel: RegisterViewModel
    private lateinit var binding: RegisterFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)

        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.register_fragment, container, false)

        binding.viewModel = viewModel

        binding.buttonRegister.setOnClickListener {
            FieldsValidator.isNicknameValid(binding.textInputLayoutNickname)
            FieldsValidator.isDisplayNameValid(binding.textInputLayoutDisplayName)
        }

        viewModel.openTermsAndConditions.observe(viewLifecycleOwner, Observer {
            if (it == true){
                openTermsAndConditions()
                viewModel.onTermsAndConditionsLaunched()
            }
        })
        return binding.root
    }

    private fun openTermsAndConditions(){
        val termsAndConditionsUri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, termsAndConditionsUri)
        if (intent.resolveActivity(context!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        when(newConfig.hardKeyboardHidden){
            Configuration.HARDKEYBOARDHIDDEN_NO -> binding.guideline6.setGuidelinePercent(0f)
            Configuration.HARDKEYBOARDHIDDEN_YES -> binding.guideline6.setGuidelinePercent(0.5f)
        }
    }

}
