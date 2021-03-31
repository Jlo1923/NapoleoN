package com.naposystems.napoleonchat.ui.attachmentGalleryFolder

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
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentGalleryFoldersFragmentBinding
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryFolder
import com.naposystems.napoleonchat.model.attachment.gallery.GalleryResult
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.ui.attachmentGalleryFolder.adapter.AttachmentGalleryFolderAdapter
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getFolders(args.location == Constants.LocationImageSelectorBottomSheet.CONVERSATION.location)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_gallery_folders_fragment, container, false
        )
        setupAdapter()

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { eventContact ->
                    args.contact?.let { noNullContact ->
                        if (noNullContact.id == eventContact.contactId) {
                            if (noNullContact.stateNotification) {
                                Utils.deleteUserChannel(
                                    requireContext(),
                                    noNullContact.id,
                                    noNullContact.getNickName()
                                )
                            }
                            findNavController().popBackStack(R.id.homeFragment, false)
                        }
                    }
                }

        disposable.add(disposableContactBlockOrDelete)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setToolbarTitle()

        viewModel.galleryFolders.observe(viewLifecycleOwner, Observer {
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

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
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
                    displayName =
                        contact.nicknameFake
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
                            args.location,
                            args.message
                        )
                    )
                }
            })

        binding.recyclerViewFolders.adapter = adapter
    }
}
