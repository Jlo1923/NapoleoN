package com.naposystems.napoleonchat.ui.attachmentPreview

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentPreviewFragmentBinding
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.custom.inputPanel.InputPanelWidget
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.sharedViewModels.contactProfile.ContactProfileSharedViewModel
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationSharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class AttachmentPreviewFragment : BaseFragment(), InputPanelWidget.Listener {

    companion object {
        fun newInstance() = AttachmentPreviewFragment()
    }

    private val conversationSharedViewModel: ConversationSharedViewModel by activityViewModels()

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    private val contactProfileSharedViewModel: ContactProfileSharedViewModel by activityViewModels {
        viewModelFactory
    }
    private lateinit var binding: AttachmentPreviewFragmentBinding
    private var isPlayingVideo: Boolean = false
    private var hasSentAttachment: Boolean = false
    private val args: AttachmentPreviewFragmentArgs by navArgs()
    private val attachmentEntity: AttachmentEntity by lazy {
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

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

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

        binding.viewModel = conversationSharedViewModel
        binding.galleryItemId = args.galleryItemId
        binding.inputPanel.setListener(this)

        conversationSharedViewModel.setAttachmentTaken(attachmentEntity)

        when (attachmentEntity.type) {
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
                        subFolder = Constants.CacheDirectories.VIDEOS.folder,
                        fileName = attachmentEntity.fileName
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

        binding.imageButtonClose.setOnClickListener {
            deleteFile()
            findNavController().navigateUp()
        }

        binding.containerVideoView.setOnClickListener {
            if (!isPlayingVideo) {
//                binding.imageViewPlay.startAnimation(animationFadeOut)
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

        if (args.message.isNotEmpty()) {
            binding.inputPanel.getEditText().setText(args.message)
        }

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { eventContact ->
                    contactProfileSharedViewModel.contact.value?.let { contact ->
                        if (contact.id == eventContact.contactId) {
                            if (contact.stateNotification) {
                                handlerNotificationChannel.deleteUserChannel(
                                    contact.id,
                                    contact.getNickName()
                                )
                            }
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                    }
                }

        disposable.add(disposableContactBlockOrDelete)

        binding.inputPanel.setEditTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit


            override fun afterTextChanged(s: Editable?) {
                binding.inputPanel.apply {
                    val text = getEditText().text.toString()
                    if (text.isNotEmpty()) containerWrap() else containerNoWrap()
                }
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        deleteFile()
        disposable.dispose()
        conversationSharedViewModel.resetAttachmentTaken()
        super.onDestroy()
    }

    private fun showPlayButton() {
//        binding.imageViewPlay.startAnimation(animationFadeIn)
        binding.imageViewPlay.visibility = View.VISIBLE
        binding.videoView.pause()
    }

    private fun setSeekbarProgress() {
        binding.seekbar.progress = binding.videoView.currentPosition
    }

    private fun deleteFile() {
        if (!hasSentAttachment) {
            FileManager.deleteAttachmentFile(requireContext(), attachmentEntity)
        }
    }

    //region Implementation InputPanelWidget.Listener
    override fun checkRecordAudioPermission(successCallback: () -> Unit) {
    }

    override fun onRecorderStarted() {
    }

    override fun onRecorderReleased() {
    }

    override fun onRecorderLocked() {
    }

    override fun onRecorderCanceled() {
    }

    override fun onSendButtonClicked() {
        with(conversationSharedViewModel) {
            setQuoteWebId(args.quote)
            setMessage(binding.inputPanel.getEditText().text.toString().trim())
            setAttachmentSelected(args.attachment)
            resetAttachmentSelected()
            resetMessage()
            resetQuoteWebId()
            hasSentAttachment = true
        }
        this.findNavController().popBackStack(R.id.conversationFragment, false)
    }
    //endregion
}
