package com.naposystems.pepito.ui.contactProfile.adapter

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.entity.Contact

@SuppressLint("SetTextI18n")
@BindingAdapter("nicknameContact")
fun bindNickNameContact(textView: TextView, @Nullable contact: Contact?) {
    if (contact != null) {
        when {
            contact.nicknameFake.isNotEmpty() -> {
                textView.text = contact.nicknameFake
            }
            contact.nickname.isNotEmpty() -> {
                textView.text = contact.nickname
            }
        }
    }
}

