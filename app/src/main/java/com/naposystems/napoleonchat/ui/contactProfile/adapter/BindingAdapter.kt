package com.naposystems.napoleonchat.ui.contactProfile.adapter

import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.entity.ContactEntity

@BindingAdapter("nicknameContact")
fun bindNickNameContact(textView: TextView, @Nullable contact: ContactEntity?) {
    textView.text = contact?.getNickName()
}

@BindingAdapter("nameContact")
fun bindNameContact(textView: TextView, @Nullable contact: ContactEntity?) {
    val context = textView.context
    if (contact != null) {

        if (contact.displayNameFake.count() < 2) {
            textView.text = ""
            textView.hint = context.getString(R.string.text_display_name)
        } else {
            textView.text = contact.getName()
        }

    }
}

