package com.naposystems.pepito.ui.recoveryOlderAccountQuestions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RecoveryOlderAccountQuestionsFragmentBinding
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RecoveryOlderAccountQuestionsFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryOlderAccountQuestionsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: RecoveryOlderAccountQuestionsViewModel by viewModels {
        viewModelFactory
    }
    private lateinit var binding: RecoveryOlderAccountQuestionsFragmentBinding

    private val args: RecoveryOlderAccountQuestionsFragmentArgs by navArgs()
    private lateinit var nickname: String
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
            R.layout.recovery_older_account_questions_fragment,
            container,
            false
        )

        nickname = args.nickname

        viewModel.getOlderQuestions(nickname)

        binding.buttonRecoveryAccount.setOnClickListener {
            viewModel.sendAnswers(
                nickname,
                binding.textInputEditTextAnswerOne.text.toString(),
                binding.textInputEditTextAnswerTwo.text.toString()
            )
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack(R.id.recoveryAccountFragment, false)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.recoveryOlderAccountQuestions.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.textViewQuestionOne.text = it.firstQuestion
                binding.textViewQuestionTwo.text = it.secondQuestion

                viewModel.resetRecoveryQuestions()
            }
        })

        viewModel.accountCreatedSuccess.observe(viewLifecycleOwner, Observer {
            it.let {
                findNavController().navigate(
                    RecoveryOlderAccountQuestionsFragmentDirections
                        .actionRecoveryOlderAccountQuestionsFragmentToAccessPinFragment(
                            it.nick,
                            it.fullname,
                            true
                        )
                )
            }
        })

        viewModel.recoveryOlderQuestionsCreatingError.observe(viewLifecycleOwner, Observer {
            binding.viewSwitcherRecoveryAccount.showNext()
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })

        viewModel.recoveryAnswersCreatingErrors.observe(viewLifecycleOwner, Observer {
            binding.viewSwitcherRecoveryAccount.showPrevious()
            viewModel.setAttemptPref()
            generalDialog(
                getString(R.string.text_alert_failure),
                getString(R.string.text_answers_incorrect),
                false,
                childFragmentManager
            ) {
                findNavController().popBackStack(R.id.recoveryAccountFragment, false)
            }
        })

        viewModel.recoveryOlderAnswersCreatingError.observe(viewLifecycleOwner, Observer {
            binding.viewSwitcherRecoveryAccount.showNext()
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })
    }

}
