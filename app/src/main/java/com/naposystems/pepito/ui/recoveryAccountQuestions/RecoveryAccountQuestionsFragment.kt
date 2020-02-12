package com.naposystems.pepito.ui.recoveryAccountQuestions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.radiobutton.MaterialRadioButton
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RecoveryAccountQuestionsFragmentBinding
import com.naposystems.pepito.model.recoveryAccount.RecoveryQuestions
import com.naposystems.pepito.model.recoveryAccountQuestions.RecoveryAccountAnswers
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils.Companion.generalDialog
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RecoveryAccountQuestionsFragment : Fragment() {

    companion object {
        fun newInstance() = RecoveryAccountQuestionsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: RecoveryAccountQuestionsViewModel
    private lateinit var binding: RecoveryAccountQuestionsFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private val args: RecoveryAccountQuestionsFragmentArgs by navArgs()

    private var indexQuestion = 0
    private var maxQuestions = 0
    private lateinit var question: RecoveryQuestions
    private var selectedAnswer: String = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (args.questions.isNotEmpty()) {
            maxQuestions = args.questions.size
            question = args.questions[indexQuestion]
        }

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.recovery_account_questions_fragment,
            container,
            false
        )

        binding.textViewControlQuestion.text =
            getString(
                R.string.text_count_questions_recovery_account, indexQuestion + 1, maxQuestions
            )

        if (args.questions.isNotEmpty()) {//Obtener Preguntas y respuestas por primera vez
            getQuestionAndAnswers(inflater, container)
        }

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedAnswer = binding.radioGroupOptions.findViewById<MaterialRadioButton>(checkedId)
                .text.toString()

            if (selectedAnswer != "") {
                binding.buttonNextStep.isEnabled = true
            }
        }

        binding.buttonNextStep.setOnClickListener {

            if (indexQuestion < maxQuestions) {
                saveQuestionAndAnswers(question.questionId, selectedAnswer)
                getQuestionAndAnswers(inflater, container)
            } else {
                saveQuestionAndAnswers(question.questionId, selectedAnswer)
                viewModel.sendRecoveryAnswers(args.nickname)
            }

            if (indexQuestion == maxQuestions) {
                binding.buttonNextStep.text = getText(R.string.text_finish)
            }
            binding.buttonNextStep.isEnabled = false

        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(RecoveryAccountQuestionsViewModel::class.java)

        viewModel.userAccountDisplayName.observe(viewLifecycleOwner, Observer { fullName ->
            if (fullName.isNotEmpty()) {
                //Dialogo de creacción de cuenta
                generalDialog(
                    getString(R.string.text_title_success),
                    getString(R.string.text_recovery_success),
                    false,
                    childFragmentManager
                ) {
                    findNavController().navigate(
                        RecoveryAccountQuestionsFragmentDirections
                            .actionRecoveryAccountQuestionsFragmentToAccessPinFragment(
                                args.nickname,
                                fullName,
                                true
                            )
                    )
                }
            }
        })



        viewModel.recoveryAnswerCreatingErrors.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
            disableRadioGroup()
            binding.buttonNextStep.isEnabled = false
        })
    }

    private fun disableRadioGroup() {
        val radioGroup = binding.radioGroupOptions

        for (i in 0 until radioGroup.childCount) {
            radioGroup.getChildAt(i).isEnabled = false
        }
    }

    private fun getQuestionAndAnswers(inflater: LayoutInflater, container: ViewGroup?) {
        question = args.questions[indexQuestion]
        binding.radioGroupOptions.removeAllViews()
        binding.textViewQuestion.text = args.questions[indexQuestion].question
        binding.textViewControlQuestion.text =
            getString(
                R.string.text_count_questions_recovery_account, indexQuestion + 1, maxQuestions
            )

        for ((index, answer) in question.answer.withIndex()) {

            val radioButtonItemAnswer = inflater.inflate(
                R.layout.recovery_account_questions_item,
                container, false
            ) as MaterialRadioButton

            radioButtonItemAnswer.id = View.generateViewId()
            radioButtonItemAnswer.tag = index

            radioButtonItemAnswer.text = answer

            binding.radioGroupOptions.addView(radioButtonItemAnswer)
        }
        indexQuestion++
    }

    private fun saveQuestionAndAnswers(questionId: Int, answer: String) {

        val recoveryAnswer = RecoveryAccountAnswers(questionId, answer)

        viewModel.addRecoveryAnswer(recoveryAnswer)
    }
}
