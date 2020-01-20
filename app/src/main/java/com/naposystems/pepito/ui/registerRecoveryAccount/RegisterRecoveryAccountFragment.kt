package com.naposystems.pepito.ui.registerRecoveryAccount

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.naposystems.pepito.databinding.RegisterRecoveryAccountBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RegisterRecoveryAccountFragment : Fragment() {

    companion object {
        fun newInstance() =
            RegisterRecoveryAccountFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: RegisterRecoveryAccountBinding

    private lateinit var viewModel: RegisterRecoveryAccountViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.register_recovery_account, container, false
        )

        binding.textViewTermsAndConditions.setOnClickListener {
            openTermsAndConditions()
        }

        binding.buttonRegister.setOnClickListener {

            findNavController().navigate(
                RegisterRecoveryAccountFragmentDirections
                    .actionRegisterRecoveryAccountFragmentToRegisterRecoveryAccountQuestionFragment()
            )
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RegisterRecoveryAccountViewModel::class.java)

        viewModel.getRecoveryQuestionsPref()
        viewModel.recoveryQuestionsPref.observe(viewLifecycleOwner, Observer {
            if (it == Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id) {
                binding.textViewDescription.setText(R.string.text_recovery_account_ok)
                binding.buttonRegister.setText(R.string.text_edit)
            }
        })
    }


    private fun openTermsAndConditions() {
        val termsAndConditionsUri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, termsAndConditionsUri)
        if (intent.resolveActivity(context!!.packageManager) != null) {
            startActivity(intent)
        }
    }

}
