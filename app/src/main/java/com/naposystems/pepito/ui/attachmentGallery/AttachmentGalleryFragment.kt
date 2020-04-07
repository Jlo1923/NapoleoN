package com.naposystems.pepito.ui.attachmentGallery

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentGalleryFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.adapter.AttachmentGalleryAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class AttachmentGalleryFragment : Fragment(), AttachmentGalleryAdapter.ClickListener {

    companion object {
        fun newInstance() = AttachmentGalleryFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: AttachmentGalleryViewModel by viewModels {
        viewModelFactory
    }

    private val galleryShareViewModel: GalleryShareViewModel by activityViewModels()

    private lateinit var binding: AttachmentGalleryFragmentBinding
    private lateinit var adapter: AttachmentGalleryAdapter
    private lateinit var attachmentSelected: File
    private val args: AttachmentGalleryFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_gallery_fragment, container, false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        postponeEnterTransition()
        binding.recyclerViewGalleryItems.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.loadGalleryItemsByFolder(args.galleryFolder)

        viewModel.galleryItems.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.submitList(it)
            }
        })
    }

    private fun setupAdapter() {
        adapter = AttachmentGalleryAdapter(this)

        binding.recyclerViewGalleryItems.adapter = adapter
    }

    //region Implementation AttachmentGalleryAdapter.ClickListener
    override fun onClick(galleryItem: GalleryItem, imageView: ImageView) {
        args.location.let { location ->
            when (location) {
                Constants.LocationImageSelectorBottomSheet.CONVERSATION.location -> {
                    lifecycleScope.launch {
                        context?.let {context ->
                            val extras = FragmentNavigatorExtras(
                                imageView to imageView.transitionName
                            )

                            val parcelFileDescriptor =
                                context.contentResolver.openFileDescriptor(galleryItem.contentUri!!, "r")

                            val fileInputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

                            if (galleryItem.attachmentType == Constants.AttachmentType.IMAGE.type) {
                                attachmentSelected = FileManager.compressImageFromFileInputStream(
                                    context, fileInputStream
                                )
                            } else if (galleryItem.attachmentType == Constants.AttachmentType.VIDEO.type) {

                                attachmentSelected = FileManager.copyFile(
                                    context,
                                    fileInputStream,
                                    Constants.NapoleonCacheDirectories.VIDEOS.folder,
                                    "${System.currentTimeMillis()}.mp4"
                                )
                            }

                            val attachment = Attachment(
                                id = 0,
                                messageId = 0,
                                webId = "",
                                messageWebId = "",
                                type = galleryItem.attachmentType,
                                body = "",
                                uri = attachmentSelected.name,
                                origin = Constants.AttachmentOrigin.GALLERY.origin,
                                thumbnailUri = "",
                                status = Constants.AttachmentStatus.SENDING.status
                            )

                            this@AttachmentGalleryFragment.findNavController().navigate(
                                AttachmentGalleryFragmentDirections.actionAttachmentGalleryFragmentToAttachmentPreviewFragment(
                                    attachment = attachment,
                                    galleryItemId = galleryItem.id,
                                    quote = args.quoteWebId
                                ),
                                extras
                            )
                        }
                    }.let {}
                }
                else -> {
                    with(galleryShareViewModel){
                        galleryItem.contentUri?.let { uri ->
                            setImageUriSelected(uri)
                            resetUriImageSelected()
                        }
                    }
                    when(location) {
                        Constants.LocationImageSelectorBottomSheet.PROFILE.location,
                        Constants.LocationImageSelectorBottomSheet.BANNER_PROFILE.location -> {
                            findNavController().popBackStack(R.id.profileFragment, false)
                        }
                        Constants.LocationImageSelectorBottomSheet.CONTACT_PROFILE.location -> {
                            findNavController().popBackStack(R.id.contactProfileFragment, false)
                        }
                        else -> {
                            findNavController().popBackStack(R.id.appearanceSettingsFragment, false)
                        }
                    }
                }
            }
        }
    }
    //endregion
}
