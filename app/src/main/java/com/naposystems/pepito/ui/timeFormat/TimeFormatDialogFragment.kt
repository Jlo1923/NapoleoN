package com.naposystems.pepito.ui.timeFormat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.naposystems.pepito.R
import androidx.lifecycle.Observer
import com.naposystems.pepito.databinding.FragmentTimeFormatDialogBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.sharedViewModels.timeFormat.TimeFormatShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class TimeFormatDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = TimeFormatDialogFragment()
    }

    interface TimeFormatListener {
        fun onTimeFormatChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: TimeFormatShareViewModel by activityViewModels { viewModelFactory }

    private lateinit var listener: TimeFormatListener
    private lateinit var binding : FragmentTimeFormatDialogBinding
    private var format: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_time_format_dialog, container, false
        )

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            format = when (checkedId) {
                R.id.radioButton_twenty_four_hours -> Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time
                else -> Constants.TimeFormat.EVERY_TWELVE_HOURS.time
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            viewModel.setTimeFormat(format)
            listener.onTimeFormatChange()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        viewModel.getTimeFormat()

        activity?.let { activity ->
            viewModel.timeFormat.observe(activity, Observer { format ->
                setFormatInRB(format)
            })
        }
    }

    private fun setFormatInRB(format : Int) {
        when(format) {
            Constants.TimeFormat.EVERY_TWENTY_FOUR_HOURS.time -> {
                binding.radioButtonTwentyFourHours.isChecked = true
            }
            else -> {
                binding.radioButtonTwelveHours.isChecked = true
            }
        }
    }

    fun setListener(listener: TimeFormatListener) {
        this.listener = listener
    }

}
