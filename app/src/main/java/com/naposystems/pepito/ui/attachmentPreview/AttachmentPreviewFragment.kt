package com.naposystems.pepito.ui.attachmentPreview

import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.MediaController
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentPreviewFragmentBinding
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream

class AttachmentPreviewFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentPreviewFragment()
    }

    private val viewModel: AttachmentPreviewViewModel by viewModels()
    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()

    private lateinit var binding: AttachmentPreviewFragmentBinding
    private var isPlayingVideo: Boolean = false
    private val args: AttachmentPreviewFragmentArgs by navArgs()
    private val galleryItem: GalleryItem by lazy {
        args.galleryItem
    }
    private val animationFadeIn: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
            R.anim.fade_in_fast
        )
    }
    private val animationFadeOut: Animation by lazy {
        AnimationUtils.loadAnimation(
            context!!,
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

        binding.galleryItem = args.galleryItem
        binding.executePendingBindings()

        if (galleryItem.contentUri != null) {
            conversationShareViewModel.setMediaUri(args.galleryItem.contentUri!!.path!!)
            conversationShareViewModel.setMediaThumbnailUri(args.galleryItem.thumbnailUri!!.path!!)

            GlobalScope.launch {
                val fileDescriptor = context!!.contentResolver
                    .openAssetFileDescriptor(args.galleryItem.contentUri!!, "r")
                val fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)

                conversationShareViewModel.setMediaBase64(
                    Utils.convertFileInputStreamToBase64(
                        fileInputStream
                    )
                )
            }
        }

        when (galleryItem.mediaType) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> {
                if (binding.viewSwitcher.currentView.id == binding.containerVideoView.id) {
                    binding.viewSwitcher.showNext()
                }
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                binding.containerSeekbar.visibility = View.VISIBLE

                if (binding.viewSwitcher.currentView.id == binding.imageViewPreview.id) {
                    binding.viewSwitcher.showNext()
                }

                binding.videoView.apply {
                    setVideoURI(galleryItem.contentUri)
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

        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            conversationShareViewModel.setMessage(binding.inputPanel.getEditTex().text.toString())

            val attachmentType: String = when (galleryItem.mediaType) {
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> Constants.AttachmentType.IMAGE.type
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> Constants.AttachmentType.VIDEO.type
                else -> ""
            }

            conversationShareViewModel.setGalleryTypeSelected(attachmentType)
            conversationShareViewModel.resetGalleryTypeSelected()
            conversationShareViewModel.resetMessage()
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

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        return binding.root
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
