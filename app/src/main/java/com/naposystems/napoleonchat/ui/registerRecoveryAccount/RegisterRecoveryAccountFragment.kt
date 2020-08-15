package com.naposystems.napoleonchat.ui.registerRecoveryAccount

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.RegisterRecoveryAccountBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
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
    private val viewModel: RegisterRecoveryAccountViewModel by viewModels { viewModelFactory }

    private var recoveryQuestionsPref: Int = 0

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
            gotoRegisterQuestions()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getRecoveryQuestionsPref()
        viewModel.recoveryQuestionsPref.observe(viewLifecycleOwner, Observer {
            recoveryQuestionsPref = it
            if (recoveryQuestionsPref == Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id) {
                binding.textViewDescription.setText(R.string.text_recovery_account_ok)
                binding.buttonRegister.setText(R.string.text_edit)
            }
        })
    }

    private fun gotoRegisterQuestions(){
        if (recoveryQuestionsPref == Constants.RecoveryQuestionsSaved.SAVED_QUESTIONS.id) {
            Utils.generalDialog(
                getString(R.string.text_alert_failure),
                getString(R.string.text_recovery_account_ok),
                true,
                childFragmentManager
            ) {
                registerQuestions()
            }
        } else {
            registerQuestions()
        }
    }

    private fun registerQuestions(){
        findNavController().navigate(
            RegisterRecoveryAccountFragmentDirections
                .actionRegisterRecoveryAccountFragmentToRegisterRecoveryAccountQuestionFragment()
        )
    }

    private fun openTermsAndConditions() {
        val termsAndConditionsUri: Uri = Uri.parse(Constants.URL_TERMS_AND_CONDITIONS)
        val intent = Intent(Intent.ACTION_VIEW, termsAndConditionsUri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }

}
