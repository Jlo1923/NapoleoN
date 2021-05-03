package com.naposystems.napoleonchat.ui.dialog.deletionMesssages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.DeletionMessagesDialogFragmentBinding

class DeletionMessagesDialogFragment(
    private val clickDeleteConversation: (Boolean) -> Unit,
    private val clickDeleteUnreads: (Boolean) -> Unit,
    private val clickDeleteUnreceived: (Boolean) -> Unit,
    private val clickDeleteFiled: (Boolean) -> Unit
) : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = DeletionMessagesDialogFragment(clickDeleteFiled = {},
            clickDeleteUnreceived = {},
            clickDeleteUnreads = {},
            clickDeleteConversation = {})
    }

    private lateinit var binding: DeletionMessagesDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.deletion_messages_dialog_fragment, container, false
        )

        binding.optionDeleteConversation.setOnClickListener {
            clickDeleteConversation(true)
        }

        binding.optionDeleteUnreadMessages.setOnClickListener {
            clickDeleteUnreads(true)
        }

        binding.optionDeleteMessagesUnreceived.setOnClickListener {
            clickDeleteUnreceived(true)
        }

        binding.optionDeleteFiledMessages.setOnClickListener {
            clickDeleteFiled(true)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation
    }

}
