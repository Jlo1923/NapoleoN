package com.naposystems.pepito.ui.securitySettings.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naposystems.pepito.R
import com.naposystems.pepito.ui.selfDestructTime.IContractSelfDestructTime
import com.naposystems.pepito.utility.Constants

@BindingAdapter("selfDestructTime")
fun bindSelfDestructTime(textView: TextView, selfDestructTime: Int) {
    val context = textView.context

    val newSelfDestructTime = when (selfDestructTime) {
        Constants.SelfDestructTime.EVERY_FIVE_MINUTES.time ->
            context.getString(R.string.text_every_five_minutes)

        Constants.SelfDestructTime.EVERY_FIFTEEN_MINUTES.time ->
            context.getString(R.string.text_every_fifteen_minutes)

        Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time ->
            context.getString(R.string.text_every_thirty_minutes)

        Constants.SelfDestructTime.EVERY_ONE_HOUR.time ->
            context.getString(R.string.text_every_one_hour)

        Constants.SelfDestructTime.EVERY_SIX_HOURS.time ->
            context.getString(R.string.text_every_six_hours)

        Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS.time ->
            context.getString(R.string.text_every_twenty_four_hours)

        else -> context.getString(R.string.text_every_twenty_four_hours)
    }

    textView.text = newSelfDestructTime
}