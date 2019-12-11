package com.naposystems.pepito.ui.profile.adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R

@BindingAdapter("status")
fun bindStatus(textView: TextView, status: String) {
    val context = textView.context
    var newStatus = status

    if (newStatus.isEmpty()) {
        newStatus = context.getString(R.string.label_share_a_state)
    }

    textView.text = newStatus
}