package com.naposystems.pepito.ui.selfDestructTime

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
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

    private var contactId: Int = 0

    companion object {

        private const val CONTACT_ID: String = "CONTACT_ID"

        fun newInstance(contactId : Int) = SelfDestructTimeDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(CONTACT_ID, contactId)
            }
        }
    }

    interface SelfDestructTimeListener {
        fun onSelfDestructTimeChange(selfDestructTimeSelected : Int)
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

        arguments.let {
            contactId = it!!.getInt(CONTACT_ID)
        }

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selfDestructTime = when (checkedId) {
                R.id.radioButton_five_seconds -> Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time
                R.id.radioButton_fifteen_seconds -> Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time
                R.id.radioButton_thirty_seconds -> Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time
                R.id.radioButton_one_minute -> Constants.SelfDestructTime.EVERY_ONE_MINUTE.time
                R.id.radioButton_ten_minutes -> Constants.SelfDestructTime.EVERY_TEN_MINUTES.time
                R.id.radioButton_thirty_minutes -> Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time
                R.id.radioButton_one_hour -> Constants.SelfDestructTime.EVERY_ONE_HOUR.time
                R.id.radioButton_twelve_hours -> Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time
                R.id.radioButton_one_day -> Constants.SelfDestructTime.EVERY_ONE_DAY.time
                else -> Constants.SelfDestructTime.EVERY_SEVEN_DAY.time
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            if(contactId == 0) {
                viewModel.setSelfDestructTime(this.selfDestructTime)
            }
            listener.onSelfDestructTimeChange(this.selfDestructTime)
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

        viewModel.getSelfDestructTimeByContact(contactId)

        viewModel.getSelfDestructTime()

        viewModel.getDestructTimeByContact.observe(viewLifecycleOwner, Observer {timeByContact ->

                this.selfDestructTime = if(contactId == 0)
                    viewModel.selfDestructTimeGlobal
                else {
                    if(timeByContact < 0)
                        viewModel.selfDestructTimeGlobal
                    else
                        timeByContact
                }
                when (this.selfDestructTime) {
                    Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time ->
                        binding.radioButtonFiveSeconds.isChecked = true

                    Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time ->
                        binding.radioButtonFifteenSeconds.isChecked = true

                    Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time ->
                        binding.radioButtonThirtySeconds.isChecked = true

                    Constants.SelfDestructTime.EVERY_ONE_MINUTE.time ->
                        binding.radioButtonOneMinute.isChecked = true

                    Constants.SelfDestructTime.EVERY_TEN_MINUTES.time ->
                        binding.radioButtonTenMinutes.isChecked = true

                    Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time ->
                        binding.radioButtonThirtyMinutes.isChecked = true

                    Constants.SelfDestructTime.EVERY_ONE_HOUR.time ->
                        binding.radioButtonOneHour.isChecked = true

                    Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time ->
                        binding.radioButtonTwelveHours.isChecked = true

                    Constants.SelfDestructTime.EVERY_ONE_DAY.time ->
                        binding.radioButtonOneDay.isChecked = true

                    Constants.SelfDestructTime.EVERY_SEVEN_DAY.time ->
                        binding.radioButtonSevenDays.isChecked = true
                }
        })
    }

    fun setListener(listener: SelfDestructTimeListener) {
        this.listener = listener
    }

}
