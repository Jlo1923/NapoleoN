package com.naposystems.pepito.ui.registerRecoveryAccountQuestion

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.RegisterRecoveryAccountQuestionFragmentBinding
import com.naposystems.pepito.entity.Questions
import com.naposystems.pepito.entity.RecoveryAnswer
import com.naposystems.pepito.ui.deleteQuestions.DeleteQuestionsDialogFragment
import com.naposystems.pepito.ui.infoAlert.InfoAlertFragment
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class RegisterRecoveryAccountQuestionFragment : Fragment() {

    companion object {
        fun newInstance() = RegisterRecoveryAccountQuestionFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: RegisterRecoveryAccountQuestionViewModel
    private lateinit var binding: RegisterRecoveryAccountQuestionFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private var countAnswer = 1
    private var maxAnswer = 3

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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

        binding.textInputEditTextAnswers.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.buttonNext.isEnabled = s!!.length >= 2
                binding.buttonAddQuestion.isEnabled = s.length >= 2
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //Nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Nothing
            }
        })


        binding.buttonAddQuestion.setOnClickListener(nextStep(1))

        binding.buttonNext.setOnClickListener(nextStep(0))

        binding.buttonCancel.setOnClickListener {
            deleteQuestions()
        }

        binding.imageButtonQuestionIcon.setOnClickListener {
            infoQuestions()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory)
                .get(RegisterRecoveryAccountQuestionViewModel::class.java)

        viewModel.getQuestions()

        viewModel.questions.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {

                val adapter = ArrayAdapter<Questions>(
                    context!!,
                    R.layout.register_recovery_account_question_item,
                    R.id.textView_question_item,
                    it
                )

                binding.spinnerQuestions.adapter = adapter
            }
        })

        viewModel.countAnswers.observe(viewLifecycleOwner, Observer {
            countAnswer = it
        })

        viewModel.maxAnswers.observe(viewLifecycleOwner, Observer {
            maxAnswer = it
        })

        viewModel.recoveryQuestionsSavedSuccessfully.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Toast.makeText(context!!, getString(R.string.text_successful_message), Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            } else if (it == false) {
                Toast.makeText(context!!, getString(R.string.text_error_message), Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.recoveryAnswerCreatingErrors.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            snackbarUtils = SnackbarUtils(binding.coordinator, it)
            snackbarUtils.showSnackbar()
        })
    }

    private fun nextStep(flag: Int) = View.OnClickListener {
        val selectedIdQuestion = binding.spinnerQuestions.selectedItemId
        val selectedQuestion = binding.spinnerQuestions.selectedItem

        //El valor por defecto es 0, por ello se valida que sea diferente de 0
        if (selectedIdQuestion.toInt() != 0) {

            if (selectedQuestion is Questions) {
                val recoveryAnswer = RecoveryAnswer(
                    selectedQuestion.id,
                    binding.textInputEditTextAnswers.text.toString()
                )
                viewModel.addRecoveryAnswer(recoveryAnswer)

                //Bandera para modificar el boton siguiente
                if (countAnswer > 3) maxAnswer = this.countAnswer

                if (countAnswer in 3..4) {
                    binding.buttonAddQuestion.isVisible = true
                    binding.buttonNext.setText(R.string.text_finish)
                } else {
                    binding.buttonAddQuestion.isVisible = false
                }

                if (flag == 0) {
                    if (countAnswer > 3) {
                        viewModel.sendRecoveryAnswers()
                    }
                }

                binding.textViewControlQuestion.text = getString(
                    R.string.text_count_questions, countAnswer, maxAnswer
                )
                binding.textInputEditTextAnswers.setText("")
            }
        } else {
            Utils.showSimpleSnackbar(coordinator, getString(R.string.text_select_question), 3)
        }
    }

    private fun infoQuestions() {
        val dialog = InfoAlertFragment()
        dialog.show(childFragmentManager, "InfoQuestions")
    }

    private fun deleteQuestions() {
        val dialog = DeleteQuestionsDialogFragment()
        dialog.setListener(object : DeleteQuestionsDialogFragment.DeleteQuestionsListener {
            override fun onDeleteQuestionsChange() {
                findNavController().popBackStack()
            }
        })
        dialog.show(childFragmentManager, "DeleteQuestions")
    }
}
