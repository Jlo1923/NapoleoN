package com.naposystems.pepito.ui.unlockAppTime

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.UnlockAppTimeFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class UnlockAppTimeFragment : Fragment() {

    companion object {
        fun newInstance() = UnlockAppTimeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: UnlockAppTimeFragmentBinding
    private lateinit var viewModel: UnlockAppTimeViewModel

    private var unlockTime = 0L
    private var lockTime = 0L

    private var hour = 0L
    private var minutes = 0L
    private var seconds = 0L
    private var showTime = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.unlock_app_time_fragment, container, false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(UnlockAppTimeViewModel::class.java)

        viewModel.getUnlockTime()
        viewModel.unlockTimeApp.observe(viewLifecycleOwner, Observer {
            unlockTime = it

            lockTime = unlockTime.minus(System.currentTimeMillis())
            unlockTime(lockTime)
        })
    }

    override fun onResume() {
        super.onResume()
        Utils.hideKeyboard(binding.container)
    }

    private fun unlockTime(time: Long) {
        val timer = object : CountDownTimer(time, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                showTime = ""
                hour = ((millisUntilFinished / 1000) / 60) / 60
                minutes = ((millisUntilFinished / 1000) / 60) % 60
                seconds = (millisUntilFinished / 1000) % 60

                showTime += if(hour < 10) "0${hour}:" else "$hour:"
                showTime += if(minutes < 10) "0${minutes}:" else "$minutes:"
                showTime += if(seconds < 10) "0${seconds}" else "$seconds"

                binding.textViewTime.text = showTime
            }

            override fun onFinish() {
                //reiniciar preferencias
                viewModel.setAttempts(0)
                viewModel.setLockType(Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type)

                findNavController().navigate(
                    UnlockAppTimeFragmentDirections.actionUnlockAppTimeFragmentToEnterPinFragment()
                )
            }
        }
        timer.start()
    }
}
