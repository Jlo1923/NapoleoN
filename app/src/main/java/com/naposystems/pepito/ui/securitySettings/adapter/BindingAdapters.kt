package com.naposystems.pepito.ui.securitySettings.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.switchmaterial.SwitchMaterial
import com.naposystems.pepito.R
import com.naposystems.pepito.utility.Constants

@BindingAdapter("selfDestructTime")
fun bindSelfDestructTime(textView: TextView, selfDestructTime: Int) {
    val context = textView.context

    val idText = when (selfDestructTime) {
        Constants.SelfDestructTime.EVERY_FIVE_SECONDS.time ->
            R.string.text_every_five_seconds

        Constants.SelfDestructTime.EVERY_FIFTEEN_SECONDS.time ->
            R.string.text_every_fifteen_seconds

        Constants.SelfDestructTime.EVERY_THIRTY_SECONDS.time ->
            R.string.text_every_thirty_seconds

        Constants.SelfDestructTime.EVERY_ONE_MINUTE.time ->
            R.string.text_every_one_minute

        Constants.SelfDestructTime.EVERY_TEN_MINUTES.time ->
            R.string.text_every_ten_minutes

        Constants.SelfDestructTime.EVERY_THIRTY_MINUTES.time ->
            R.string.text_every_thirty_minutes

        Constants.SelfDestructTime.EVERY_ONE_HOUR.time ->
            R.string.text_every_one_hour

        Constants.SelfDestructTime.EVERY_TWELVE_HOURS.time ->
            R.string.text_every_twelve_hours

        Constants.SelfDestructTime.EVERY_ONE_DAY.time ->
            R.string.text_every_one_day

        Constants.SelfDestructTime.EVERY_SEVEN_DAY.time ->
            R.string.text_every_seven_days

        else -> R.string.text_every_seven_days
    }

    textView.text = context.getString(idText)
}

@BindingAdapter("timeRequestAccessPin")
fun bindTimeRequestAccessPin(textView: TextView, time: Int) {
    val context = textView.context

    val idText = when (time) {
        Constants.TimeRequestAccessPin.IMMEDIATELY.time ->
            R.string.text_immediately

        Constants.TimeRequestAccessPin.TEN_SECONDS.time ->
            R.string.text_ten_seconds

        Constants.TimeRequestAccessPin.ONE_MINUTE.time ->
            R.string.text_one_minute

        Constants.TimeRequestAccessPin.FIVE_MINUTES.time ->
            R.string.text_five_minutes

        Constants.TimeRequestAccessPin.ONE_HOUR.time ->
            R.string.text_one_hour

        Constants.TimeRequestAccessPin.ONE_DAY.time ->
            R.string.text_one_day

        Constants.TimeRequestAccessPin.NEVER.time ->
            R.string.text_never

        else -> R.string.text_thirty_seconds
    }

    textView.text = context.getString(idText)
}

@BindingAdapter("allowDownload")
fun bindAllowDownload(switch: SwitchMaterial, state: Int) {

    switch.isChecked = when (state) {
        Constants.AllowDownloadAttachments.YES.option -> true
        Constants.AllowDownloadAttachments.NO.option -> false
        else -> false
    }
}

@BindingAdapter("messageSelfDestructTimeNotSent")
fun bindMessageSelfDestructTimeNotSent(textView: TextView, selfDestructTimeMessageNotSent: Int) {
    val context = textView.context

    val selfDestructTimeMessageNotSent = when (selfDestructTimeMessageNotSent) {
        Constants.MessageSelfDestructTimeNotSent.TWENTY_FOUR.time ->
            context.getString(R.string.text_every_twenty_four_hours)
        else ->
            context.getString(R.string.text_every_seven_days)
    }

    textView.text = selfDestructTimeMessageNotSent
}