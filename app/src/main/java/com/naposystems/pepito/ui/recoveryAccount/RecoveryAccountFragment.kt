package com.naposystems.pepito.ui.recoveryAccount

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RecoveryAccountFragmentBinding
import com.naposystems.pepito.model.recoveryAccount.ListRecoveryQuestions
import com.naposystems.pepito.model.recoveryAccount.RecoveryQuestions
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RecoveryAccountFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryAccountFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: RecoveryAccountFragmentBinding
    private lateinit var viewModel: RecoveryAccountViewModel

    private lateinit var snackbarUtils: SnackbarUtils

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recovery_account_fragment, container, false
        )

        binding.textInputEditTextNickname.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonRecoveryAccount.isEnabled = s!!.length >= 4
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
            }
        })

        binding.buttonRecoveryAccount.setOnClickListener {
            Utils.hideKeyboard(binding.textInputEditTextNickname)
            viewModel.sendNickname(binding.textInputEditTextNickname.text.toString())
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RecoveryAccountViewModel::class.java)


        viewModel.recoveryQuestions.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {

                val listRecoveryQuestions = ListRecoveryQuestions()

                for (recoveryQuestion in it) {
                    listRecoveryQuestions.add(recoveryQuestion)
                }

                findNavController().navigate(
                    RecoveryAccountFragmentDirections
                        .actionRecoveryAccountFragmentToRecoveryAccountQuestionsFragment(
                            listRecoveryQuestions,
                            binding.textInputEditTextNickname.text.toString()
                        )
                )
                viewModel.resetRecoveryQuestions()
            } else {
                //Snackbar
            }
        })

        viewModel.recoveryQuestionsCreatingError.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })
    }
}
