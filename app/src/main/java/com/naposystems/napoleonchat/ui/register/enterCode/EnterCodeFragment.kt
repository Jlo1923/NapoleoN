package com.naposystems.napoleonchat.ui.register.enterCode

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.EnterCodeFragmentBinding
import com.naposystems.napoleonchat.ui.custom.EnterCodeWidget
import com.naposystems.napoleonchat.ui.custom.numericKeyboard.NumericKeyboardCustomView
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.SnackbarUtils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.DaggerFragment
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//TODO: Revisar error aqui al registrar sesion
class EnterCodeFragment :
    DaggerFragment(), EnterCodeWidget.OnEventListener,
    NumericKeyboardCustomView.OnEventListener {

    companion object {
        fun newInstance() = EnterCodeFragment()
        const val MAX_ATTEMPTS = 3
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: EnterCodeViewModel by viewModels { viewModelFactory }

   // private var _binding: EnterCodeFragmentBinding? = null

   // private val binding get() = _binding!!
   private lateinit var binding : EnterCodeFragmentBinding

    private lateinit var snackbarUtils: SnackbarUtils
    private var attemptsForEnterCode: Int = 0
    private var attemptsForNewCode: Int = 0
    private var timerEnterCode: CountDownTimer? = null
    private var timerNewCode: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Bindeo del fragmento
        binding = EnterCodeFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.enterCodeWidget.setListener(this)
        binding.numericKeyboard.setListener(this)

        binding.buttonContinue.setOnClickListener {
            binding.viewSwitcher.showNext()
            viewModel.sendCode(binding.enterCodeWidget.getCode())
        }

        binding.buttonCodeForwarding.setOnClickListener {
            viewModel.codeForwarding()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.isValidCode.observe(viewLifecycleOwner, Observer { success ->
            if (success) {
                findNavController().navigate(
                    R.id.action_enterCodeFragment_to_validateNicknameFragment
                )
            }
        })

        viewModel.getAttemptsForNewCode()
        viewModel.attemptsForNewCode.observe(viewLifecycleOwner, Observer {
            attemptsForNewCode = it
            /*if (attemptsForNewCode > 0) {
                binding.textViewAttemptForNewCode.text =
                    getString(R.string.text_number_attempts, attemptsForNewCode, MAX_ATTEMPTS)
                binding.textViewAttemptForNewCode.visibility = View.VISIBLE
            } else {
                binding.textViewAttemptForNewCode.visibility = View.GONE
            }*/
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
            if (success) timeForNewCode()
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
                snackbarUtils.showSnackbar {}
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

        binding.buttonCodeForwarding.isEnabled = false
        timerNewCode = object : CountDownTimer(
            time - System.currentTimeMillis(), 1000
        ) {
            override fun onFinish() {
                if (attemptsForNewCode == MAX_ATTEMPTS &&
                    System.currentTimeMillis() > viewModel.getTimeForNewCode()
                ) {
                    viewModel.resetAttemptsNewCode()
                    viewModel.getAttemptsForNewCode()
                }
                binding.buttonCodeForwarding.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                /*if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished).toInt() > 0) {
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
                }*/
            }
        }
        timerNewCode?.start()
    }

    private fun configurationForInvalidCode() {
        binding.textViewAttemptsForEnterCode.text =
            getString(R.string.text_number_attempts, attemptsForEnterCode, MAX_ATTEMPTS)
        binding.textViewAttemptsForEnterCode.visibility = View.VISIBLE
        binding.textViewTimeForEnterCode.visibility = View.VISIBLE
        binding.numericKeyboard.visibility = View.GONE
        binding.enterCodeWidget.showError()
        binding.buttonContinue.isEnabled = false
        binding.buttonCodeForwarding.isEnabled = false
        binding.viewSwitcher.showNext()
    }

    private fun restoreConfigurationEnterCode() {
        binding.textViewTimeForEnterCode.visibility = View.GONE
        binding.numericKeyboard.visibility = View.VISIBLE
        binding.buttonContinue.isEnabled = true
        binding.buttonCodeForwarding.isEnabled = true
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
