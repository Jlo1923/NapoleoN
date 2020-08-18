package com.naposystems.napoleonchat.ui.addContact.adapter

import android.graphics.drawable.Animatable
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.button.MaterialButton
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.entity.addContact.FriendshipRequestTitle

@BindingAdapter("haveFriendshipRequest")
fun haveFriendShipRequest(button: MaterialButton, haveFriendShipRequest: Boolean) {

    val context = button.context

    val animatedVectorDrawableCompat =
        button.icon as Animatable

    if (haveFriendShipRequest) {
        animatedVectorDrawableCompat.start()
        button.setTextColor(context.getColor(R.color.green))
        button.text = context.resources.getString(R.string.text_sent)
        button.setStrokeColorResource(R.color.green)
    } else {
        button.setTextColor(context.getColor(R.color.buttonTint))
        button.text = context.resources.getString(R.string.text_add)
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