package com.naposystems.napoleonchat.ui.addContact.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.FriendshipRequestTitle

@BindingAdapter("haveFriendshipRequest")
fun haveFriendShipRequest(button: MaterialButton, haveFriendShipRequest: Boolean) {
    val context = button.context

    if (haveFriendShipRequest) {
        button.setTextColor(context.getColor(R.color.green))
        button.text = context.resources.getString(R.string.text_sent)
        button.setIconResource(R.drawable.ic_check_primary)
        button.setStrokeColorResource(R.color.green)
    } else {
        button.setTextColor(context.getColor(R.color.buttonTint))
        button.text = context.resources.getString(R.string.text_add)
        button.setIconResource(R.drawable.ic_add_primary)
        button.setStrokeColorResource(R.color.buttonTint)
    }
}

@BindingAdapter("titleFriendshipRequest")
fun bindTitleFriendshipRequest(textView: TextView, friendshipRequestTitle: FriendshipRequestTitle) {
    val context = textView.context
    val title = if (friendshipRequestTitle.id == -1) {
        R.string.text_friend_requests_received
    } else {
        R.string.text_friend_requests_sent
    }
    textView.text = context.getString(title)
}