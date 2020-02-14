package com.naposystems.pepito.ui.conversation.adapter

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.User
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


@BindingAdapter("messageDate")
fun bindMessageDate(textView: TextView, timestamp: Int) {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm aa", Locale.getDefault())
        val netDate = Date(timestamp.toLong() * 1000)
        textView.text = sdf.format(netDate)
        textView.visibility = View.VISIBLE
    } catch (e: Exception) {
        Timber.d("Error parsing date")
    }
}

@BindingAdapter("conversationBackground")
fun bindConversationBackground(appCompatImageView: AppCompatImageView, user: User) {

    val context = appCompatImageView.context
    val yourDrawable: Drawable
    val uri = Uri.parse(user.chatBackground)

    if (user.chatBackground.isNotEmpty()) {

        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)!!
            yourDrawable = Drawable.createFromStream(inputStream, uri.toString())
            appCompatImageView.setImageURI(uri)
        } catch (e: FileNotFoundException) {
            val color = context.resources.getColor(R.color.colorBackground, context.theme)
            appCompatImageView.setBackgroundColor(color)
        }
    }
}