package com.naposystems.pepito.ui.contactProfile.adapter

import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact

@BindingAdapter("nicknameContact")
fun bindNickNameContact(textView: TextView, @Nullable contact: Contact?) {
    textView.text = contact?.getNickName()
}

@BindingAdapter("nameContact")
fun bindNameContact(textView: TextView, @Nullable contact: Contact?) {
    val context = textView.context
    if (contact != null) {
        when {
            contact.displayNameFake.isNotEmpty() -> {
                if (contact.displayNameFake.count() < 2) {
                    textView.text = ""
                    textView.hint = context.getString(R.string.text_display_name)
                } else {
                    textView.text = contact.displayNameFake
                }
            }
            contact.displayName.isNotEmpty() -> {
                textView.text = contact.displayName
            }
        }
    }
}

