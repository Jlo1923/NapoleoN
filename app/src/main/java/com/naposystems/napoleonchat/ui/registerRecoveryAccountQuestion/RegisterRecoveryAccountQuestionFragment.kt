package com.naposystems.napoleonchat.ui.registerRecoveryAccountQuestion

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.RegisterRecoveryAccountQuestionFragmentBinding
import com.naposystems.napoleonchat.entity.Questions
import com.naposystems.napoleonchat.entity.RecoveryAnswer
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.Utils.Companion.generalDialog
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.*
import javax.inject.Inject


class RegisterRecoveryAccountQuestionFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterRecoveryAccountQuestionFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: RegisterRecoveryAccountQuestionViewModel by viewModels { viewModelFactory }
    private lateinit var binding: RegisterRecoveryAccountQuestionFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private var countAnswer = 1
    private var maxAnswer = 3

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            showDeleteQuestionsDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val actionBar = (activity as MainActivity).supportActionBar
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayHomeAsUpEnabled(false)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.register_recovery_account_question_fragment,
            container,
            false
        )

        binding.textViewControlQuestion.text = getString(
            R.string.text_count_questions, countAnswer, maxAnswer
        )

        binding.buttonAddQuestion.setOnClickListener {
            registerQuestion(1)
        }

        binding.buttonNext.setOnClickListener {
            registerQuestion(0)
        }

        binding.buttonCancel.setOnClickListener {
            showDeleteQuestionsDialog()
        }

        binding.imageButtonQuestionIcon.setOnClickListener {
            infoQuestions()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getQuestions()

        viewModel.questions.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {

                val selectQuestion = requireContext().getString(R.string.text_security_questions)

                val newListQuestion = it.toMutableList()
                newListQuestion.add(0, Questions(0, selectQuestion))

                val adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.register_recovery_account_question_item,
                    R.id.textView_question_item,
                    newListQuestion
                )

                binding.spinnerQuestions.adapter = adapter
            }
        })

        viewModel.countAnswers.observe(viewLifecycleOwner, Observer {
            countAnswer = it
        })

        viewModel.recoveryQuestionsSavedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.text_successful_message),
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
            } else if (it == false) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.text_error_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        viewModel.recoveryAnswerCreatingErrors.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar {}
        })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar {}
        })
    }

    private fun registerQuestion(flag: Int) {
        val selectedIdQuestion = binding.spinnerQuestions.selectedItemId
        val selectedQuestion = binding.spinnerQuestions.selectedItem
        val textInputAnswer =
            binding.textInputEditTextAnswers.text.toString().trim().replace("\\s+".toRegex(), " ")

        if (selectedIdQuestion.toInt() == 0 && textInputAnswer.isEmpty() && countAnswer > 3) {
            generalDialog(
                getString(R.string.text_title_info),
                getString(R.string.text_final_register),
                true,
                childFragmentManager
            ) {
                viewModel.sendRecoveryAnswers()
            }
        } else {
            if (selectedIdQuestion.toInt() != 0) {
                verifyAnswers(selectedQuestion, textInputAnswer, flag)
            } else {
                Toast.makeText(
                    requireContext(), getString(R.string.text_select_question), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun verifyAnswers(selectedQuestion: Any?, textInputAnswer: String, flag: Int) {
        if (textInputAnswer.isNotEmpty()) {
            binding.textInputLayoutAnswers.error = null
            if (selectedQuestion is Questions) {
                val recoveryAnswer = RecoveryAnswer(
                    selectedQuestion.id,
                    binding.textInputEditTextAnswers.text.toString().trim().toLowerCase(Locale.ROOT)
                )
                if (flag == 0) {
                    if (countAnswer >= 3) {
                        viewModel.addRecoveryAnswer(recoveryAnswer, 0)
                        viewModel.sendRecoveryAnswers()
                    } else
                        viewModel.addRecoveryAnswer(recoveryAnswer, 1)
                } else {
                    viewModel.addRecoveryAnswer(recoveryAnswer, 1)
                }

                if (countAnswer > 3) maxAnswer = this.countAnswer
                binding.textViewControlQuestion.text = getString(
                    R.string.text_count_questions, countAnswer, maxAnswer
                )

                if (countAnswer in 3..4) {
                    binding.buttonAddQuestion.isVisible = true
                    binding.buttonNext.setText(R.string.text_finish)
                } else {
                    binding.buttonAddQuestion.isVisible = false
                }
                binding.textInputEditTextAnswers.setText("")
            }
        } else {
            binding.textInputLayoutAnswers.error = getString(R.string.text_field_cannot_be_empty)
        }
    }

    private fun infoQuestions() {
        generalDialog(
            getString(R.string.text_title_info),
            getString(R.string.text_info_register_account),
            false,
            childFragmentManager
        ) {}
    }

    private fun showDeleteQuestionsDialog() {
        generalDialog(
            getString(R.string.text_title_cancel),
            getString(R.string.text_description_cancel),
            true,
            childFragmentManager,
            getString(R.string.text_confirm),
            getString(R.string.text_close)
        ) {
            findNavController().popBackStack()
        }
    }
}
