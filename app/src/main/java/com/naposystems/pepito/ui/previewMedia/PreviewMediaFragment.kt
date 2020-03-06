package com.naposystems.pepito.ui.previewMedia

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.PreviewMediaFragmentBinding
import com.naposystems.pepito.entity.message.MessageAndAttachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.Utils

class PreviewMediaFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewMediaFragment()
    }

    private val viewModel: PreviewMediaViewModel by viewModels()
    private lateinit var binding: PreviewMediaFragmentBinding
    private val args: PreviewMediaFragmentArgs by navArgs()
    private var isPlayingVideo: Boolean = false
    private var isUIVisible: Boolean = true
    private val messageAndAttachment: MessageAndAttachment by lazy {
        args.messageAndAttachment
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.preview_media_fragment,
            container,
            false
        )

        binding.message = args.messageAndAttachment

        if (args.messageAndAttachment.message.body.isEmpty()) {
            binding.containerMessageAndSeekbar.visibility = View.GONE
        }

        val firstAttachment = messageAndAttachment.attachmentList[0]

        when (firstAttachment.type) {
            Constants.AttachmentType.IMAGE.type -> {
                if (binding.viewSwitcher.currentView.id == binding.containerVideoView.id) {
                    binding.viewSwitcher.showNext()
                }
            }
            Constants.AttachmentType.VIDEO.type -> {
                binding.containerSeekbar.visibility = View.VISIBLE

                if (binding.viewSwitcher.currentView.id == binding.imageViewPreview.id) {
                    binding.viewSwitcher.showNext()
                }

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    ContentUris.parseId(Uri.parse(firstAttachment.uri))
                )

                binding.videoView.apply {
                    setVideoURI(contentUri)
                    requestFocus()
                    seekTo(1)

                    setOnPreparedListener {
                        binding.seekbar.max = duration
                        startVideo()
                        isPlayingVideo = !isPlayingVideo
                    }

                    setOnCompletionListener {
                        this@PreviewMediaFragment.isPlayingVideo = false
                        if (::mRunnable.isInitialized) {
                            mHandler.removeCallbacks(mRunnable)
                        }

                        binding.videoView.seekTo(1)
                        binding.seekbar.apply {
                            progress = 0
                            max = duration
                        }

                        binding.imageButtonPlay.reverseAnimation()
                        showUI()
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

        binding.imageButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.container.setOnClickListener {
            if (isUIVisible) {
                hideUI()
            } else {
                showUI()
            }
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

        binding.imageButtonPlay.setOnClickListener {
            if (!isPlayingVideo) {
                startVideo()
            } else {
                binding.imageButtonPlay.apply {
                    reverseAnimation()
                }
                binding.videoView.pause()
            }

            isPlayingVideo = !isPlayingVideo
        }

        binding.executePendingBindings()

        return binding.root
    }

    private fun setSeekbarProgress() {
        binding.seekbar.progress = binding.videoView.currentPosition
    }

    /*
     * Ocultamos la cabecera y el seekbar
     */
    private fun hideUI() {
        binding.imageButtonPlay.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        binding.containerHeader.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        binding.containerMessageAndSeekbar.apply {
            startAnimation(animationFadeOut)
            visibility = View.GONE
        }
        isUIVisible = !isUIVisible
    }

    /*
     * Mostramos la cabecera y el seekbar
     */
    private fun showUI() {
        binding.containerHeader.apply {
            startAnimation(animationFadeIn)
            visibility = View.VISIBLE
        }
        binding.containerMessageAndSeekbar.apply {
            startAnimation(animationFadeIn)
            visibility = View.VISIBLE
        }
        if (binding.imageButtonPlay.visibility == View.GONE) {
            binding.imageButtonPlay.apply {
                startAnimation(animationFadeIn)
                visibility = View.VISIBLE
            }
        }

        isUIVisible = !isUIVisible
    }

    private fun startVideo() {
        binding.imageButtonPlay.playAnimation()
        hideUI()
        binding.videoView.start()
        mHandler.postDelayed(mRunnable, 0)
    }

}
