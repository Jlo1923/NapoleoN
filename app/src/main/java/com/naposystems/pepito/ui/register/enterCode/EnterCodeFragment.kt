package com.naposystems.pepito.ui.register.enterCode

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EnterCodeFragmentBinding
import com.naposystems.pepito.ui.custom.EnterCodeWidget
import com.naposystems.pepito.ui.custom.numericKeyboard.NumericKeyboardCustomView
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EnterCodeFragment :
    Fragment(), EnterCodeWidget.OnEventListener,
    NumericKeyboardCustomView.OnEventListener {

    companion object {
        fun newInstance() = EnterCodeFragment()
        const val MAX_ATTEMPTS = 3
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: EnterCodeViewModel by viewModels { viewModelFactory }
    private lateinit var binding: EnterCodeFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private var attemptsForEnterCode: Int = 0
    private var attemptsForNewCode: Int = 0
    private var timerEnterCode: CountDownTimer? = null
    private var timerNewCode: CountDownTimer? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.enter_code_fragment, container, false
        )

        binding.enterCodeWidget.setListener(this)
        binding.numericKeyboard.setListener(this)

        binding.buttonContinue.setOnClickListener {
            binding.viewSwitcher.showNext()
            viewModel.sendCode(binding.enterCodeWidget.getCode())
        }

        binding.buttonCodeForwarding.setOnClickListener {
            viewModel.codeForwarding()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isValidCode.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                findNavController().navigate(
                    EnterCodeFragmentDirections.actionEnterCodeFragmentToRegisterFragment()
                )
            }
        })

        viewModel.getAttemptsForNewCode()
        viewModel.attemptsForNewCode.observe(viewLifecycleOwner, Observer {
            attemptsForNewCode = it
            if (attemptsForNewCode > 0) {
                binding.textViewAttemptForNewCode.text =
                    getString(R.string.text_number_attempts, attemptsForNewCode, MAX_ATTEMPTS)
                binding.textViewAttemptForNewCode.visibility = View.VISIBLE
            } else {
                binding.textViewAttemptForNewCode.visibility = View.GONE
            }
        })

        viewModel.getAttemptsForRetryCode()
        viewModel.attemptsEnterCode.observe(viewLifecycleOwner, Observer {
            attemptsForEnterCode = it
            if (attemptsForEnterCode > 0) {
                binding.textViewAttemptsForEnterCode.text =
                    getString(R.string.text_number_attempts, attemptsForEnterCode, MAX_ATTEMPTS)
                binding.textViewAttemptsForEnterCode.visibility = View.VISIBLE
            }
        })

        viewModel.forwardedCode.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                timeForNewCode()
            }
        })

        viewModel.invalidCode.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                configurationForInvalidCode()
                timeForRetryCode()
            }
        })

        viewModel.responseErrors.observe(viewLifecycleOwner, Observer {
            if (!it.isNullOrEmpty()) {
                binding.viewSwitcher.showNext()
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        timeForNewCode()
    }

    private fun timeForRetryCode() {
        val time = if (attemptsForEnterCode == MAX_ATTEMPTS)
            viewModel.setTimeForRetryCode(Constants.TimeSendCode.THIRTY_SECONDS.time)
        else
            viewModel.setTimeForRetryCode(Constants.TimeSendCode.TEN_SECONDS.time)

        if (attemptsForEnterCode == MAX_ATTEMPTS) {
            findNavController().popBackStack()
        } else {
            timerEnterCode = object : CountDownTimer(
                time - System.currentTimeMillis(), 1000
            ) {
                override fun onFinish() {
                    restoreConfigurationEnterCode()
                }

                override fun onTick(millisUntilFinished: Long) {
                    binding.textViewTimeForEnterCode.text =
                        if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() >= 60) {
                            resources.getQuantityString(
                                R.plurals.text_new_attempt_in_x_minutes,
                                TimeUnit.MILLISECONDS.toSeconds(
                                    millisUntilFinished
                                ).toInt() / 60,
                                TimeUnit.MILLISECONDS.toSeconds(
                                    millisUntilFinished
                                ).toInt() / 60
                            )
                        } else {
                            resources.getQuantityString(
                                R.plurals.text_new_attempt_in_x_seconds,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt(),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
                            )
                        }
                }
            }
            timerEnterCode?.start()
        }
    }

    private fun timeForNewCode() {
        val time = if (viewModel.getNumAttemptsForNewCode() == MAX_ATTEMPTS)
            viewModel.setTimeForNewCode(Constants.TimeSendCode.FIVE_MINUTES.time)
        else
            viewModel.setTimeForNewCode(Constants.TimeSendCode.THIRTY_SECONDS.time)

        binding.textViewTimeForNewCode.visibility = View.VISIBLE
        binding.buttonCodeForwarding.isEnabled = false
        timerNewCode = object : CountDownTimer(
            time - System.currentTimeMillis(), 1000
        ) {
            override fun onFinish() {
                binding.textViewTimeForNewCode.visibility = View.GONE
                if (attemptsForNewCode == MAX_ATTEMPTS &&
                    System.currentTimeMillis() > viewModel.getTimeForNewCode()
                ) {
                    viewModel.resetAttemptsNewCode()
                    viewModel.getAttemptsForNewCode()
                }
                binding.buttonCodeForwarding.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() > 0) {
                    binding.textViewTimeForNewCode.text =
                        if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() >= 60) {
                            resources.getQuantityString(
                                R.plurals.text_new_attempt_in_x_minutes,
                                TimeUnit.MILLISECONDS.toSeconds(
                                    millisUntilFinished
                                ).toInt() / 60,
                                TimeUnit.MILLISECONDS.toSeconds(
                                    millisUntilFinished
                                ).toInt() / 60
                            )
                        } else {
                            resources.getQuantityString(
                                R.plurals.text_new_attempt_in_x_seconds,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt(),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt()
                            )
                        }
                }
            }
        }
        timerNewCode?.start()
    }

    private fun configurationForInvalidCode() {
        binding.textViewAttemptsForEnterCode.text =
            getString(R.string.text_number_attempts, attemptsForEnterCode, MAX_ATTEMPTS)
        binding.textViewAttemptsForEnterCode.visibility = View.VISIBLE
        binding.textViewError.visibility = View.VISIBLE
        binding.textViewTimeForEnterCode.visibility = View.VISIBLE
        binding.numericKeyboard.visibility = View.GONE
        binding.enterCodeWidget.showError()
        binding.buttonContinue.isEnabled = false
        binding.viewSwitcher.showNext()
    }

    private fun restoreConfigurationEnterCode() {
        binding.textViewError.visibility = View.GONE
        binding.textViewTimeForEnterCode.visibility = View.GONE
        binding.numericKeyboard.visibility = View.VISIBLE
        binding.buttonContinue.isEnabled = true
        binding.enterCodeWidget.deleteNumber()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerEnterCode?.cancel()
        timerNewCode?.cancel()
    }


    //region Implementation EnterCodeWidget.OnEventListener
    override fun onImeActionDone() {
        viewModel.sendCode(binding.enterCodeWidget.getCode())
    }

    override fun onCodeCompleted(isCompleted: Boolean) {
        binding.buttonContinue.isEnabled = isCompleted
    }

    //endregion

    //region Implementation NumericKeyboardCustomView.OnEventListener
    override fun onKeyPressed(keyCode: Int) {
        binding.enterCodeWidget.setAddNumber(keyCode)

        binding.numericKeyboard.showDeleteKey(binding.enterCodeWidget.getCode().isNotEmpty())
    }

    override fun onDeletePressed() {
        binding.enterCodeWidget.deleteNumber()
    }
    //endregion
}
