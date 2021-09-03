package com.naposystems.napoleonchat.ui.selfDestructTimeMessageNotSentFragment

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
import com.naposystems.napoleonchat.databinding.SelfDestructTimeMessageNotSentDialogFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class SelfDestructTimeMessageNotSentDialogFragment : DialogFragment() {

    companion object {
        fun newInstance() = SelfDestructTimeMessageNotSentDialogFragment()
    }

    interface MessageSelfDestructTimeNotSentListener {
        fun onDestructMessageChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: SelfDestructTimeMessageNotSentViewDialogModel by viewModels { viewModelFactory }
    private lateinit var binding: SelfDestructTimeMessageNotSentDialogFragmentBinding
    private lateinit var listener: MessageSelfDestructTimeNotSentListener

    private var messageTimeNotSent: Int = 0

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.self_destruct_time_message_not_sent_dialog_fragment, container, false
        )

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            messageTimeNotSent = when (checkedId) {
                R.id.radioButton_seven_days_destruct_message ->
                    Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time
                else -> Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time
            }
        }

        binding.buttonAccept.setOnClickListener {
            viewModel.setSelfDestructTimeMessageNotSent(messageTimeNotSent)
            listener.onDestructMessageChange()
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
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

        viewModel.getSelfDestructTimeMessageNotSent()

        viewModel.selfDestructTimeMessage.observe(viewLifecycleOwner, Observer {
            this.messageTimeNotSent = it

            when (it) {
                Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time ->
                    binding.radioButtonTwentyFourHoursDestructMessage.isChecked = true
                Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time ->
                    binding.radioButtonSevenDaysDestructMessage.isChecked = true
            }
        })
    }

    fun setListener(listener: MessageSelfDestructTimeNotSentListener) {
        this.listener = listener
    }
}
