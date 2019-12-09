package com.naposystems.pepito.ui.register.enterCode

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EnterCodeFragmentBinding
import com.naposystems.pepito.dto.enterCode.EnterCodeReqDTO
import com.naposystems.pepito.ui.custom.EnterCodeWidget
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.SharedPreferencesManager
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EnterCodeFragment : Fragment(), EnterCodeWidget.OnEventListener {

    companion object {
        fun newInstance() = EnterCodeFragment()
        const val MAX_ATTEMPTS = 3
        const val NEW_ATTEMPT_IN: Long = 30
    }

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: EnterCodeViewModel
    private lateinit var binding: EnterCodeFragmentBinding
    private lateinit var errorList: List<String>
    private lateinit var snackbar: Snackbar
    private var countDownTimer: CountDownTimer? = null
    private var errorIndex: Int = 0
    private var hasFinishedShowingErrors: Boolean = false

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(EnterCodeViewModel::class.java)

        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.enter_code_fragment, container, false
        )

        viewModel.attempts.observe(viewLifecycleOwner, Observer {
            binding.textViewAttempts.apply {
                text = resources.getString(R.string.number_of_attempts, it, MAX_ATTEMPTS)
                visibility = if (it >= 1) View.VISIBLE else View.GONE
            }
            if (it == MAX_ATTEMPTS) {
                disableAllWidgets()
            } else {
                if (it >= 1) {
                    disableAllWidgets()
                    timerToEnableWidgets()
                }
            }
        })

        viewModel.showInvalidCode.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                binding.viewSwitcher.showPrevious()
                viewModel.increaseAttempts()
                binding.enterCodeWidget.showError()
            }
        })

        viewModel.showErrors.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                binding.viewSwitcher.showPrevious()
                viewModel.increaseAttempts()
                binding.enterCodeWidget.showError()
                errorList = it
                hasFinishedShowingErrors = false
                showSnackbar()
            }
        })

        viewModel.itsCodeOk.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                sharedPreferencesManager.putInt(
                    Constants.SharedPreferences.PREF_ACCOUNT_STATUS,
                    Constants.AccountStatus.CODE_VALIDATED.id
                )
                findNavController().navigate(
                    EnterCodeFragmentDirections.actionEnterCodeFragmentToRegisterFragment()
                )
            }
        })

        binding.enterCodeWidget.setListener(this)

        binding.buttonContinue.setOnClickListener {
            sendCodeToWs(binding.enterCodeWidget.getCode())
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.enterCodeWidget.requestFocusFirst()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }

    private fun showSnackbar() {
        snackbar =
            Snackbar.make(binding.coordinator, errorList[errorIndex], Snackbar.LENGTH_INDEFINITE)
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        snackbarErrorHandler()
                    }
                })
                .setAction(R.string.okay) {
                    snackbarErrorHandler()
                }
        val snackbarView = snackbar.view

        val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 5

        snackbar.show()
    }

    private fun snackbarErrorHandler() {
        if (errorIndex < errorList.size - 1 && !hasFinishedShowingErrors) {
            errorIndex += 1
            showSnackbar()
        } else {
            errorIndex = 0
            hasFinishedShowingErrors = true
        }
    }

    private fun sendCodeToWs(code: String) {
        binding.viewSwitcher.showNext()

        val firebaseId = sharedPreferencesManager
            .getString(Constants.SharedPreferences.PREF_FIREBASE_ID, "")

        val enterCodeReqDTO = EnterCodeReqDTO(
            firebaseId,
            code
        )
        viewModel.onContinueButtonPressed(enterCodeReqDTO)
    }

    private fun timerToEnableWidgets() {
        binding.textViewNewAttemptIn.text = resources.getQuantityString(
            R.plurals.new_attempt_in_x_seconds,
            NEW_ATTEMPT_IN.toInt(),
            NEW_ATTEMPT_IN
        )
        binding.textViewNewAttemptIn.visibility = View.VISIBLE

        countDownTimer =
            object : CountDownTimer(TimeUnit.SECONDS.toMillis(NEW_ATTEMPT_IN), 1000) {
                override fun onFinish() {
                    binding.textViewNewAttemptIn.visibility = View.GONE
                    viewModel.resetShowInvalidCode()
                    enableAllWidgets()
                }

                override fun onTick(millisUntilFinished: Long) {
                    val untilFinished: Int = (millisUntilFinished / 1000).toInt() + 1

                    binding.textViewNewAttemptIn.text = resources.getQuantityString(
                        R.plurals.new_attempt_in_x_seconds,
                        untilFinished,
                        untilFinished
                    )
                }
            }.start()
    }

    private fun disableAllWidgets() {
        binding.enterCodeWidget.disableTextInput()
        binding.buttonDidntReceiveCode.isEnabled = false
        binding.buttonContinue.isEnabled = false
    }

    private fun enableAllWidgets() {
        binding.enterCodeWidget.enableTextInput()
        binding.buttonDidntReceiveCode.isEnabled = true
        binding.buttonContinue.isEnabled = true
    }

    //region Implementation EnterCodeWidget.OnEventListener
    override fun onImeActionDone() {
        sendCodeToWs(binding.enterCodeWidget.getCode())
    }

    override fun onCodeCompleted(isCompleted: Boolean) {
        binding.buttonContinue.isEnabled = isCompleted
    }

    //endregion
}
