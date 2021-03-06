package com.naposystems.napoleonchat.ui.attachmentAudio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentAudioFragmentBinding
import com.naposystems.napoleonchat.model.MediaStoreAudio
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.service.notificationClient.NotificationClient
import com.naposystems.napoleonchat.ui.attachmentAudio.adapter.AttachmentAudioAdapter
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerGalleryManager
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationSharedViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class AttachmentAudioFragment : BaseFragment(), MediaPlayerGalleryManager.Listener {

    companion object {
        fun newInstance() = AttachmentAudioFragment()
    }

    @Inject
    override lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var notificationClient: NotificationClient

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    @Inject
    lateinit var mediaPlayerGalleryManager: MediaPlayerGalleryManager

    private lateinit var viewModel: AttachmentAudioViewModel
    private lateinit var conversationSharedViewModel: ConversationSharedViewModel
    private lateinit var binding: AttachmentAudioFragmentBinding
    private lateinit var adapter: AttachmentAudioAdapter
    private val args: AttachmentAudioFragmentArgs by navArgs()

    private var chargeContent = false

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private val animationScaleUp: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.scale_up
        )
    }

    private val animationScaleDown: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.scale_down
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_audio_fragment, container, false
        )

        binding.lifecycleOwner = this

        binding.fabSend.setOnClickListener {
            conversationSharedViewModel.setAudiosSelected(viewModel.getAudiosSelected())
            conversationSharedViewModel.setAudioSendClicked()
            conversationSharedViewModel.resetAudioSendClicked()
            findNavController().navigateUp()
        }

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (args.contact.id == it.contactId) {
                        if (args.contact.stateNotification) {
                            handlerNotificationChannel.deleteUserChannel(
                                args.contact.id,
                                args.contact.getNickName()
                            )
                        }
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }

        disposable.add(disposableContactBlockOrDelete)

        mediaPlayerGalleryManager.setListener(this)

        setupAdapter()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //region AttachmentAudioViewModel
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AttachmentAudioViewModel::class.java)

        viewModel.loadAudios()

        viewModel.audios.observe(viewLifecycleOwner, Observer {
            setToolbarTitle(it)
            adapter.submitList(it.toList())
            if (!chargeContent) {
                binding.viewSwitcher.showNext()
                chargeContent = true
            }
        })
        //endregion

        //region ConversationShareViewModel
        conversationSharedViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(ConversationSharedViewModel::class.java)
        //endregion
    }

    private fun setToolbarTitle(listMediaStoreAudio: List<MediaStoreAudio>) {
        val toolbar = (activity as MainActivity).supportActionBar
        val countSelected = listMediaStoreAudio.filter {
            it.isSelected
        }.count()

        val contact = args.contact

        val displayName = if (contact.displayNameFake.isNotEmpty())
            contact.displayNameFake else contact.displayName

        toolbar?.title = getString(R.string.text_send_to, displayName)
        toolbar?.subtitle = if (countSelected > 0)
            resources.getQuantityString(
                R.plurals.text_selected,
                countSelected,
                countSelected
            ) else getString(R.string.text_tap_to_select)

        if (countSelected == 1 && binding.fabSend.visibility == View.INVISIBLE) {
            binding.fabSend.startAnimation(animationScaleUp)
            binding.fabSend.visibility = View.VISIBLE
        }

        if (binding.fabSend.visibility == View.VISIBLE && countSelected == 0) {
            binding.fabSend.startAnimation(animationScaleDown)
            binding.fabSend.visibility = View.INVISIBLE
        }
    }

    private fun setupAdapter() {
        adapter = AttachmentAudioAdapter(object : AttachmentAudioAdapter.ClickListener {
            override fun onClick(mediaStoreAudio: MediaStoreAudio) {
                viewModel.setSelected(mediaStoreAudio)
            }

            override fun onPlayClick(
                mediaStoreAudio: MediaStoreAudio,
                imageButtonPlay: ImageView
            ) {
                mediaPlayerGalleryManager.apply {
                    setAudioId(mediaStoreAudio.id.toString())
                    setImageButtonPlay(imageButtonPlay)
                    setAudioUri(mediaStoreAudio.contentUri)
                    playAudio()
                }
            }
        }, mediaPlayerGalleryManager)

        binding.recyclerViewAudios.adapter = adapter
        binding.recyclerViewAudios.itemAnimator = ItemAnimator()
    }

    override fun onDestroy() {
        mediaPlayerGalleryManager.resetMediaPlayer()
        disposable.dispose()
        super.onDestroy()
    }

    //region Implementation _root_ide_package_.com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerGalleryManager.Listener
    override fun onErrorPlayingAudio() {
        Utils.showSimpleSnackbar(
            binding.coordinator,
            getString(R.string.text_error_playing_audio),
            3
        )
    }

    override fun onCompleteAudio(messageId: String) {
        adapter.notifyDataSetChanged()
    }

    //endregion
}
