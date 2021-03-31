package com.naposystems.napoleonchat.ui.addContact.viewHolder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AddContactRequestReceivedItemBinding
import com.naposystems.napoleonchat.model.addContact.Contact
import com.naposystems.napoleonchat.ui.addContact.adapter.AddContactAdapter
import com.naposystems.napoleonchat.utils.handlerDialog.HandlerDialog

class RequestContactReceivedHolder
constructor(
    private val binding: AddContactRequestReceivedItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: Contact,
        clickListener: AddContactAdapter.ClickListener,
        fragmentManager: FragmentManager,
        handlerDialog: HandlerDialog,
        context: Context
    ) {
        binding.apply {
            contact = item
            progressbar.isVisible = false
            containerButtons.isVisible = true

            buttonRefuse.setOnClickListener {

                handlerDialog.generalDialog(
                    context.getString(R.string.text_friend_title_reject_request),
                    context.getString(
                        R.string.text_friend_text_reject_request,
                        item.nickname
                    ),
                    true,
                    fragmentManager,
                    context.getString(
                        R.string.text_confirm
                    )
                ) {
                    hideButtons()
                    item.statusFriend = false
                    item.offer = false
                    clickListener.onAcceptRequest(item, false)
                }


            }
            buttonAccept.setOnClickListener {
                hideButtons()
                item.statusFriend = true
                item.offer = false
                clickListener.onAcceptRequest(item, true)
            }
            executePendingBindings()
        }

    }

    fun hideButtons() {
        binding.progressbar.isVisible = true
        binding.containerButtons.isVisible = false
    }

    companion object {
        fun from(parent: ViewGroup): RequestContactReceivedHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = AddContactRequestReceivedItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
            return RequestContactReceivedHolder(binding)
        }
    }
}