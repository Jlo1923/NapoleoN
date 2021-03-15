package com.naposystems.napoleonchat.ui.addContact.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.model.FriendshipRequestTitle

@BindingAdapter("avatar")
fun bindAvatar(imageView: ImageView, @Nullable loadImage: String?) {
    val context = imageView.context
    val defaultAvatar = ContextCompat.getDrawable(context, R.drawable.ic_default_avatar)

    Glide.with(context)
        .load(loadImage)
        .apply(
            RequestOptions()
                .priority(Priority.NORMAL)
                .fitCenter()
        ).error(defaultAvatar)
        .circleCrop()
        .into(imageView)

}

@BindingAdapter("name")
fun bindName(textView: TextView, @Nullable name: String) {
    textView.text = name
}

@BindingAdapter("nickname")
fun bindNickname(textView: TextView, @Nullable nickName: String?) {
    textView.context.let { context ->
        textView.text = context.getString(R.string.label_nickname, nickName)
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

@BindingAdapter("visible")
fun View.visible(isVisible: Boolean?) {
    visibility = if (isVisible == true) View.VISIBLE else View.GONE
}