package com.naposystems.napoleonchat.ui.multipreview.fragments

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.databinding.FragmentMultipleAttachmentPreviewImageBinding
import com.naposystems.napoleonchat.ui.multi.model.MultipleAttachmentFileItem
import com.naposystems.napoleonchat.ui.multipreview.listeners.MultipleAttachmentPreviewListener
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.extensions.hide
import java.util.concurrent.TimeUnit

class MultipleAttachmentPreviewImageFragment(
    val file: MultipleAttachmentFileItem,
    val position: Int
) : Fragment() {

    private lateinit var binding: FragmentMultipleAttachmentPreviewImageBinding
    private var listener: MultipleAttachmentPreviewListener? = null

    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultipleAttachmentPreviewImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.imagePreview.setOnClickListener { listener?.changeVisibilityOptions() }
        if (file.messageAndAttachment == null) {
            loadImage()
        } else {
            loadImageFromBody()
        }

        configTimer()
    }

    private fun configTimer() {

        countDownTimer?.cancel()
        file.messageAndAttachment?.let {
            val endTime = it.attachment.totalSelfDestructionAt
            if (endTime > 0) {
                val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
                val remainingTime = endTime - currentTime

                countDownTimer = object : CountDownTimer(
                    TimeUnit.SECONDS.toMillis(endTime) - System.currentTimeMillis(),
                    1
                ) {
                    override fun onFinish() {
                        listener?.deleteAttachmentByDestructionTime(it.attachment, position)
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        val text = Utils.getTimeWithDays(millisUntilFinished, showHours = true)
                        binding.textTimeAutodestruction.text = text
                    }
                }

                countDownTimer?.start()
            } else {
                binding.textTimeAutodestruction.hide()
            }
        } ?: run { binding.textTimeAutodestruction.hide() }

    }

    private fun loadImageFromBody() {
        try {
            binding.apply {
                Glide.with(root.context)
                    .load(file.messageAndAttachment?.attachment?.body)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    private fun loadImage() {
        try {
            binding.apply {
                Glide.with(root.context).load(file.contentUri)
                    .into(imagePreview)
            }
        } catch (exception: Exception) {

        }
    }

    fun setListener(listener: MultipleAttachmentPreviewListener) {
        this.listener = listener
    }

    override fun onPause() {
        super.onPause()
        binding.apply { imagePreview.fitToScreen() }
    }

}