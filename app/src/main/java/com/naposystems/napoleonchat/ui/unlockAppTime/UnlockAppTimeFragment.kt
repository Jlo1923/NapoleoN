package com.naposystems.napoleonchat.ui.unlockAppTime

import android.annotation.SuppressLint
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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.UnlockAppTimeFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class UnlockAppTimeFragment : Fragment() {

    companion object {
        fun newInstance() = UnlockAppTimeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var binding: UnlockAppTimeFragmentBinding
    private val viewModel: UnlockAppTimeViewModel by viewModels { viewModelFactory }

    private var unlockTime = 0L
    private var lockTime = 0L

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
                binding.textViewTime.text = Utils.getDuration(millisUntilFinished)
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
