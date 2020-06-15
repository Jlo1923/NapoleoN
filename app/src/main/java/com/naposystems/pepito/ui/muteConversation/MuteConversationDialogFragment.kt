package com.naposystems.pepito.ui.muteConversation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ConversationMuteDialogFragmentBinding
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

private const val ID_CONTACT = "idContact"
private const val CONTACT_SILENCED = "contactSilenced"

class MuteConversationDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(idContact: Int, contactSilenced: Boolean) =
            MuteConversationDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ID_CONTACT, idContact)
                    putBoolean(CONTACT_SILENCED, contactSilenced)
                }
            }
    }

    interface MuteConversationListener {
        fun onMuteConversationChange()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: MuteConversationViewModel by viewModels { viewModelFactory }
    private lateinit var binding: ConversationMuteDialogFragmentBinding
    private lateinit var listener: MuteConversationListener

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.conversation_mute_dialog_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            viewModel.timeMuteConversation = when (checkedId) {
                R.id.radioButton_one_hour -> Constants.TimeMuteConversation.ONE_HOUR.time
                R.id.radioButton_eight_hours -> Constants.TimeMuteConversation.EIGHT_HOURS.time
                R.id.radioButton_one_day -> Constants.TimeMuteConversation.ONE_DAY.time
                R.id.radioButton_one_year -> Constants.TimeMuteConversation.ONE_YEAR.time
                else -> Constants.TimeMuteConversation.WITHOUT_TIME.time
            }
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            if (viewModel.timeMuteConversation != Constants.TimeMuteConversation.WITHOUT_TIME.time) {
                viewModel.updateContactSilenced(
                    requireArguments().getInt(ID_CONTACT),
                    requireArguments().getBoolean(CONTACT_SILENCED)
                )
            }
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

        binding.viewmodel = viewModel

        viewModel.mutedConversation.observe(viewLifecycleOwner, Observer {
            dismiss()
        })

        viewModel.muteConversationWsError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }

    fun setListener(muteConversationListener: MuteConversationListener) {
        listener = muteConversationListener
    }

    override fun onDetach() {
        listener.onMuteConversationChange()
        super.onDetach()
    }

}
