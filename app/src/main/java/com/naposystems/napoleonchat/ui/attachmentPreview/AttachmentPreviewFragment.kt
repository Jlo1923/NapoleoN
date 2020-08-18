package com.naposystems.napoleonchat.ui.attachmentPreview

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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentPreviewFragmentBinding
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel

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

        binding.viewModel = conversationShareViewModel
        binding.galleryItemId = args.galleryItemId

        conversationShareViewModel.setAttachmentTaken(attachment)

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

                if (binding.viewSwitcher.currentView.id == binding.scrollView.id) {
                    binding.viewSwitcher.showNext()
                }

                binding.videoView.apply {

                    val fileUri = Utils.getFileUri(
                        context = requireContext(),
                        subFolder = Constants.NapoleonCacheDirectories.VIDEOS.folder,
                        fileName = attachment.fileName
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
                setMessage(binding.inputPanel.getEditTex().text.toString().trim())
                setAttachmentSelected(args.attachment)
                resetAttachmentSelected()
                resetMessage()
                resetQuoteWebId()
                hasSentAttachment = true
            }
            this.findNavController().popBackStack(R.id.conversationFragment, false)
        }

        binding.imageButtonClose.setOnClickListener {
            deleteFile()
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

        binding.scrollView.setOnClickListener {

        }

        binding.executePendingBindings()

        if (args.message.isNotEmpty()) {
            binding.inputPanel.getEditTex().setText(args.message)
        }

        return binding.root
    }

    override fun onDestroy() {
        deleteFile()
        conversationShareViewModel.resetAttachmentTaken()
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

    private fun deleteFile() {
        if (!hasSentAttachment) {
            FileManager.deleteAttachmentFile(requireContext(), attachment)
        }
    }
}
