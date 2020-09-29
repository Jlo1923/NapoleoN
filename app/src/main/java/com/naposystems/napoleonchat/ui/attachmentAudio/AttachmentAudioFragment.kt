package com.naposystems.napoleonchat.ui.attachmentAudio

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentAudioFragmentBinding
import com.naposystems.napoleonchat.entity.message.attachments.MediaStoreAudio
import com.naposystems.napoleonchat.ui.attachmentAudio.adapter.AttachmentAudioAdapter
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.ItemAnimator
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.mediaPlayer.MediaPlayerGalleryManager
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AttachmentAudioFragment : Fragment(), MediaPlayerGalleryManager.Listener {

    companion object {
        fun newInstance() = AttachmentAudioFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: AttachmentAudioViewModel
    private lateinit var conversationShareViewModel: ConversationShareViewModel
    private lateinit var binding: AttachmentAudioFragmentBinding
    private lateinit var adapter: AttachmentAudioAdapter
    private val args: AttachmentAudioFragmentArgs by navArgs()

    private var chargeContent = false


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

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
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
            conversationShareViewModel.setAudiosSelected(viewModel.getAudiosSelected())
            conversationShareViewModel.setAudioSendClicked()
            conversationShareViewModel.resetAudioSendClicked()
            findNavController().navigateUp()
        }

        MediaPlayerGalleryManager.setContext(requireContext())
        MediaPlayerGalleryManager.setListener(this)

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
        conversationShareViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(ConversationShareViewModel::class.java)
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
                MediaPlayerGalleryManager.apply {
                    setAudioId(mediaStoreAudio.id.toString())
                    setImageButtonPlay(imageButtonPlay)
                    setContext(requireContext())
                    setAudioUri(mediaStoreAudio.contentUri)
                    playAudio()
                }
                adapter.notifyDataSetChanged()
            }
        })

        binding.recyclerViewAudios.adapter = adapter
        binding.recyclerViewAudios.itemAnimator = ItemAnimator()
    }

    override fun onDestroy() {
        MediaPlayerGalleryManager.resetMediaPlayer()
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
