package com.naposystems.pepito.ui.register.sendCode

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SendCodeFragmentBinding
import com.naposystems.pepito.utility.SharedPreferencesManager
import com.naposystems.pepito.utility.SnackbarUtils
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SendCodeFragment : Fragment() {

    companion object {
        fun newInstance() = SendCodeFragment()
        const val MAX_ATTEMPTS = 3
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SendCodeViewModel by viewModels { viewModelFactory }
    private lateinit var binding: SendCodeFragmentBinding
    private lateinit var snackbarUtils: SnackbarUtils
    private var timeForNewCode = 0L
    private var timeForEnterCode = 0L
    private var countDownTimer: CountDownTimer? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.send_code_fragment, container, false
        )

        binding.viewModel = viewModel

        binding.buttonSendCode.setOnClickListener {
            binding.viewSwitcher.showNext()
            viewModel.requestCode()
        }

        viewModel.codeSuccess.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                findNavController().navigate(
                    SendCodeFragmentDirections.actionSendCodeFragmentToEnterCodeFragment()
                )
                viewModel.resetCode()
            }
        })

        viewModel.webServiceError.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                binding.viewSwitcher.showNext()
                snackbarUtils = SnackbarUtils(binding.coordinator, it)
                snackbarUtils.showSnackbar()
            }
        })

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getTimeForNewCode()
        viewModel.timeForNewCode.observe(viewLifecycleOwner, Observer {
            timeForNewCode = it
        })

        viewModel.getTimeForEnterCode()
        viewModel.timeForEnterCode.observe(viewLifecycleOwner, Observer {
            timeForEnterCode = it
            validateTypeCode()
        })
    }

    private fun validateTypeCode() {
        var timeForWait = 0L
        if (viewModel.getAttemptsEnterCode() != 0 && timeForEnterCode > System.currentTimeMillis()) {
            binding.buttonSendCode.isEnabled = false
            timeForWait = timeForEnterCode
        } else if (viewModel.getAttemptsNewCode() != 0 && timeForNewCode > System.currentTimeMillis()) {
            binding.buttonSendCode.isEnabled = false
            timeForWait = timeForNewCode
        }
        setTimeForWait(timeForWait - System.currentTimeMillis())
    }

    private fun setTimeForWait(time: Long) {
        binding.textViewTimeForNewCode.visibility = View.VISIBLE
        countDownTimer = object : CountDownTimer(time, 1000) {
            override fun onFinish() {
                binding.textViewTimeForNewCode.visibility = View.GONE
                if (viewModel.getAttemptsEnterCode() == MAX_ATTEMPTS &&
                    System.currentTimeMillis() > timeForEnterCode) {
                    viewModel.resetAttemptsEnterCode()
                }else if (viewModel.getAttemptsNewCode() == MAX_ATTEMPTS &&
                    System.currentTimeMillis() > timeForNewCode){
                    viewModel.resetAttemptsNewCode()
                }
                binding.buttonSendCode.isEnabled = true
            }

            override fun onTick(millisUntilFinished: Long) {
                binding.textViewTimeForNewCode.text =
                    Utils.getDuration(millisUntilFinished, false)
            }
        }
        countDownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
