package com.naposystems.pepito.ui.selfDestructTime

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SelfDestructTimeDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SelfDestructTimeDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = SelfDestructTimeDialogFragment()
    }

    interface SelfDestructTimeListener {
        fun onSelfDestructTimeChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SelfDestructTimeViewModel
    private lateinit var binding: SelfDestructTimeDialogFragmentBinding
    private lateinit var listener: SelfDestructTimeListener

    private var selfDestructTime: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.self_destruct_time_dialog_fragment, container, false
        )

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selfDestructTime = when (checkedId) {
                R.id.radioButton_five_minutes -> Constants.SelfDestructTime.EVERY_FIVE_MINUTES.time
                R.id.radioButton_fifteen_minutes -> Constants.SelfDestructTime.EVERY_FIFTEEN_MINUTES.time
                R.id.radioButton_thirty_minutes -> Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time
                R.id.radioButton_one_hour -> Constants.SelfDestructTime.EVERY_ONE_HOUR.time
                R.id.radioButton_six_hours -> Constants.SelfDestructTime.EVERY_SIX_HOURS.time
                R.id.radioButton_twenty_four_hours -> Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS.time
                else -> Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS.time
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            viewModel.setSelfDestructTime(this.selfDestructTime)
            listener.onSelfDestructTimeChange()
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
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(SelfDestructTimeViewModel::class.java)

        viewModel.getSelfDestructTime()

        viewModel.selfDestructTime.observe(viewLifecycleOwner, Observer {
            this.selfDestructTime = it
            when (it) {
                Constants.SelfDestructTime.EVERY_FIVE_MINUTES.time ->
                    binding.radioButtonFiveMinutes.isChecked = true

                Constants.SelfDestructTime.EVERY_FIFTEEN_MINUTES.time ->
                    binding.radioButtonFifteenMinutes.isChecked = true

                Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time ->
                    binding.radioButtonThirtyMinutes.isChecked = true

                Constants.SelfDestructTime.EVERY_ONE_HOUR.time ->
                    binding.radioButtonOneHour.isChecked = true

                Constants.SelfDestructTime.EVERY_SIX_HOURS.time ->
                    binding.radioButtonSixHours.isChecked = true

                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS.time ->
                    binding.radioButtonTwentyFourHours.isChecked = true
            }
        })

    }

    fun setListener(listener: SelfDestructTimeListener) {
        this.listener = listener
    }

}
