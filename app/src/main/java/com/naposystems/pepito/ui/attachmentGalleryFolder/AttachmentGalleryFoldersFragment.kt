package com.naposystems.pepito.ui.attachmentGalleryFolder

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentGalleryFoldersFragmentBinding
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.model.attachment.gallery.GalleryResult
import com.naposystems.pepito.ui.attachmentGalleryFolder.adapter.AttachmentGalleryFolderAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class AttachmentGalleryFoldersFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentGalleryFoldersFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: AttachmentGalleryFoldersViewModel by viewModels {
        viewModelFactory
    }
    private val args: AttachmentGalleryFoldersFragmentArgs by navArgs()
    private lateinit var binding: AttachmentGalleryFoldersFragmentBinding
    private lateinit var adapter: AttachmentGalleryFolderAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_gallery_folders_fragment, container, false
        )
        setupAdapter()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle()
        viewModel.folders.observe(viewLifecycleOwner, Observer {
            when (it) {
                is GalleryResult.Loading -> {
                    // Intentionally empty
                }
                is GalleryResult.Success -> adapter.submitList(it.listGalleryFolder)
                is GalleryResult.Error -> {
                    this.showToast(requireContext().getString(R.string.text_fail))
                    Timber.e(it.exception)
                }
            }
        })
    }

    private fun setToolbarTitle() {
        val title = when (args.location) {
            Constants.LocationImageSelectorBottomSheet.PROFILE.location -> getString(R.string.text_change_profile_photo)
            Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location -> getString(R.string.text_change_cover_photo)
            Constants.LocationImageSelectorBottomSheet.APPEARANCE_SETTINGS.location -> getString(R.string.text_conversation_background)
            Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location -> getString(R.string.text_change_contact_photo)
            Constants.LocationImageSelectorBottomSheet.CONVERSATION.location -> {
                var displayName = ""

                args.contact?.let { contact ->
                    displayName = if (contact.nicknameFake.isNotEmpty())
                        contact.nicknameFake else contact.nickname
                }
                getString(R.string.text_send_to, displayName)
            }
            else -> ""
        }

        val toolbar = (activity as MainActivity).supportActionBar
        toolbar?.title = title
    }

    private fun setupAdapter() {
        adapter =
            AttachmentGalleryFolderAdapter(object : AttachmentGalleryFolderAdapter.ClickListener {
                override fun onClick(galleryFolder: GalleryFolder) {
                    findNavController().navigate(
                        AttachmentGalleryFoldersFragmentDirections.actionAttachmentGalleryFragmentToAttachmentGalleryFragment(
                            args.contact,
                            galleryFolder,
                            args.quoteWebId,
                            args.location
                        )
                    )
                }
            })

        binding.recyclerViewFolders.adapter = adapter
    }
}
