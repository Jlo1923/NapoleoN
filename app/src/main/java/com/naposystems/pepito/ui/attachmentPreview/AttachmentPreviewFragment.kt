package com.naposystems.pepito.ui.attachmentPreview

import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentPreviewFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import java.io.File

class AttachmentPreviewFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentPreviewFragment()
    }

    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()

    private lateinit var binding: AttachmentPreviewFragmentBinding
    private var isPlayingVideo: Boolean = false
    private var hasSentAttachment: Boolean = false
    private val args: AttachmentPreviewFragmentArgs by navArgs()
    private val attachment: Attachment by lazy {
        args.attachment
    }
    private val animationFadeIn: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fade_in_fast
        )
    }
    private val animationFadeOut: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.fade_out_fast
        )
    }
    private val mHandler: Handler by lazy {
        Handler()
    }
    private lateinit var mRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_preview_fragment, container, false
        )

        binding.attachment = attachment
        binding.galleryItemId = args.galleryItemId

        when (attachment.type) {
            Constants.AttachmentType.IMAGE.type,
            Constants.AttachmentType.GIF.type,
            Constants.AttachmentType.GIF_NN.type -> {
                if (binding.viewSwitcher.currentView.id == binding.containerVideoView.id) {
                    binding.viewSwitcher.showNext()
                }
            }
            Constants.AttachmentType.VIDEO.type -> {
                binding.containerSeekbar.visibility = View.VISIBLE

                if (binding.viewSwitcher.currentView.id == binding.imageViewPreview.id) {
                    binding.viewSwitcher.showNext()
                }

                binding.videoView.apply {

                    val fileUri = Utils.getFileUri(
                        context = requireContext(),
                        subFolder = Constants.NapoleonCacheDirectories.VIDEOS.folder,
                        fileName = attachment.uri
                    )

                    setVideoURI(fileUri)
                    requestFocus()
                    seekTo(1)

                    setOnPreparedListener {
                        binding.seekbar.max = duration
                    }

                    setOnCompletionListener {
                        this@AttachmentPreviewFragment.isPlayingVideo = false
                        if (::mRunnable.isInitialized) {
                            mHandler.removeCallbacks(mRunnable)
                        }

                        binding.seekbar.apply {
                            progress = 0
                            max = duration
                        }
                        showPlayButton()
                    }

                    mRunnable = Runnable {
                        setSeekbarProgress()

                        mHandler.postDelayed(
                            mRunnable,
                            50
                        )
                    }
                }
            }
        }

        binding.floatingActionButtonSend.setOnClickListener {
            with(conversationShareViewModel) {
                setQuoteWebId(args.quote)
                setMessage(binding.inputPanel.getEditTex().text.toString())
                setAttachmentSelected(args.attachment)
                resetAttachmentSelected()
                resetMessage()
                resetQuoteWebId()
                hasSentAttachment = true
            }
            this.findNavController().popBackStack(R.id.conversationFragment, false)
        }

        binding.imageButtonClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.containerVideoView.setOnClickListener {
            if (!isPlayingVideo) {
                binding.imageViewPlay.startAnimation(animationFadeOut)
                binding.imageViewPlay.visibility = View.GONE
                binding.videoView.start()
                mHandler.postDelayed(mRunnable, 0)
            } else {
                showPlayButton()
            }

            isPlayingVideo = !isPlayingVideo
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.videoView.seekTo(progress)
                }
                binding.textViewDuration.text = Utils.getDuration(
                    (binding.videoView.duration - progress).toLong(),
                    showHours = false
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Intentionally empty.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Intentionally empty.
            }
        })

        binding.executePendingBindings()

        return binding.root
    }

    override fun onDestroy() {
        if (!hasSentAttachment) {
            val file = File(args.attachment.uri)

            if (file.exists()) {
                file.delete()
            }
        }
        super.onDestroy()
    }

    private fun showPlayButton() {
        binding.imageViewPlay.startAnimation(animationFadeIn)
        binding.imageViewPlay.visibility = View.VISIBLE
        binding.videoView.pause()
    }

    private fun setSeekbarProgress() {
        binding.seekbar.progress = binding.videoView.currentPosition
    }
}
