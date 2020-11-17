package com.naposystems.napoleonchat.ui.timeAccessPin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.TimeAccessPinDialogFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TimeAccessPinDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = TimeAccessPinDialogFragment()
    }

    interface TimeAccessPinListener {
        fun onTimeAccessChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TimeAccessPinDialogViewModel by viewModels { viewModelFactory }
    private lateinit var binding: TimeAccessPinDialogFragmentBinding
    private lateinit var listener: TimeAccessPinListener

    private var timeAccess: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.time_access_pin_dialog_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            timeAccess = when (checkedId) {
                R.id.radioButton_immediately -> Constants.TimeRequestAccessPin.IMMEDIATELY.time
                R.id.radioButton_ten_seconds -> Constants.TimeRequestAccessPin.TEN_SECONDS.time
                R.id.radioButton_thirty_seconds -> Constants.TimeRequestAccessPin.THIRTY_SECONDS.time
                R.id.radioButton_one_minute -> Constants.TimeRequestAccessPin.ONE_MINUTE.time
                R.id.radioButton_five_minutes -> Constants.TimeRequestAccessPin.FIVE_MINUTES.time
                R.id.radioButton_one_hour -> Constants.TimeRequestAccessPin.ONE_HOUR.time
                R.id.radioButton_one_day -> Constants.TimeRequestAccessPin.ONE_DAY.time
                else -> Constants.TimeRequestAccessPin.NEVER.time
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            viewModel.setTimeAccessPin(this.timeAccess)
            if (timeAccess == Constants.TimeRequestAccessPin.NEVER.time)
                viewModel.setLockType(Constants.LockTypeApp.FOREVER_UNLOCK.type)
            else
                viewModel.setLockType(Constants.LockTypeApp.LOCK_FOR_TIME_REQUEST_PIN.type)
            listener.onTimeAccessChange()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.let {
            it.attributes.windowAnimations = R.style.DialogAnimation
            it.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        binding.viewModel = viewModel

        viewModel.getTimeAccessPin()

        viewModel.timeAccessPin.observe(viewLifecycleOwner, Observer {
            this.timeAccess = it
            when (it) {
                Constants.TimeRequestAccessPin.IMMEDIATELY.time ->
                    binding.radioButtonImmediately.isChecked = true

                Constants.TimeRequestAccessPin.TEN_SECONDS.time ->
                    binding.radioButtonTenSeconds.isChecked = true

                Constants.TimeRequestAccessPin.THIRTY_SECONDS.time ->
                    binding.radioButtonThirtySeconds.isChecked = true

                Constants.TimeRequestAccessPin.ONE_MINUTE.time ->
                    binding.radioButtonOneMinute.isChecked = true

                Constants.TimeRequestAccessPin.FIVE_MINUTES.time ->
                    binding.radioButtonFiveMinutes.isChecked = true

                Constants.TimeRequestAccessPin.ONE_HOUR.time ->
                    binding.radioButtonOneHour.isChecked = true

                Constants.TimeRequestAccessPin.ONE_DAY.time ->
                    binding.radioButtonOneDay.isChecked = true

                Constants.TimeRequestAccessPin.NEVER.time ->
                    binding.radioButtonNever.isChecked = true
            }
        })
    }

    fun setListener(listener: TimeAccessPinListener) {
        this.listener = listener
    }

}
