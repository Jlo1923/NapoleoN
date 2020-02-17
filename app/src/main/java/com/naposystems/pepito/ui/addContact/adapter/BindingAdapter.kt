package com.naposystems.pepito.ui.addContact.adapter

import android.graphics.drawable.AnimatedVectorDrawable
import androidx.databinding.BindingAdapter
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.android.material.button.MaterialButton
import com.naposystems.pepito.R

@BindingAdapter("haveFriendshipRequest")
fun haveFriendShipRequest(button: MaterialButton, haveFriendShipRequest: Boolean) {

    val context = button.context

    val animatedVectorDrawableCompat =
        button.icon as AnimatedVectorDrawable

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