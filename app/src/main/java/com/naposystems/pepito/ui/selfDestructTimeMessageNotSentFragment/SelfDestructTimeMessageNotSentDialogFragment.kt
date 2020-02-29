package com.naposystems.pepito.ui.selfDestructTimeMessageNotSentFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SelfDestructTimeMessageNotSentDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
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
    private lateinit var viewModel: SelfDestructTimeMessageNotSentViewModel
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
            messageTimeNotSent = when(checkedId) {
                R.id.radioButton_seven_days_destruct_message ->
                    Constants.MessageSelfDestructTimeNotSent.SEVEN_DAYS.time
                else -> Constants.MessageSelfDestructTimeNotSent.TWENTY_FOUR.time
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
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory)
                .get(SelfDestructTimeMessageNotSentViewModel::class.java)

        viewModel.getSelfDestructTimeMessageNotSent()

        viewModel.selfDestructTimeMessage.observe(viewLifecycleOwner, Observer {
            this.messageTimeNotSent = it

            when(it) {
                Constants.MessageSelfDestructTimeNotSent.TWENTY_FOUR.time ->
                    binding.radioButtonTwentyFourHoursDestructMessage.isChecked = true
                Constants.MessageSelfDestructTimeNotSent.SEVEN_DAYS.time ->
                    binding.radioButtonSevenDaysDestructMessage.isChecked = true
            }
        })
    }

    fun setListener(listener: MessageSelfDestructTimeNotSentListener) {
        this.listener = listener
    }
}
