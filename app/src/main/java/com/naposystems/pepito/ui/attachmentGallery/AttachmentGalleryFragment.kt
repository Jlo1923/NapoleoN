package com.naposystems.pepito.ui.attachmentGallery

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentGalleryFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.adapter.AttachmentGalleryAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.sharedViewModels.gallery.GalleryShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class AttachmentGalleryFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>,
    AttachmentGalleryAdapter.ClickListener {

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
        setToolbarTitle()
        postponeEnterTransition()
        binding.recyclerViewGalleryItems.doOnPreDraw { startPostponedEnterTransition() }
        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    private fun setToolbarTitle() {
        val toolbar = (activity as MainActivity).supportActionBar
        toolbar?.title = args.galleryFolder.folderName
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
                        context?.let { context ->

                            var extension = ""

                            val extras = FragmentNavigatorExtras(
                                imageView to imageView.transitionName
                            )

                            val parcelFileDescriptor =
                                context.contentResolver.openFileDescriptor(
                                    galleryItem.contentUri!!,
                                    "r"
                                )

                            val fileInputStream =
                                FileInputStream(parcelFileDescriptor!!.fileDescriptor)

                            if (galleryItem.attachmentType == Constants.AttachmentType.IMAGE.type) {
                                extension = "jpg"
                                attachmentSelected = FileManager.compressImageFromFileInputStream(
                                    context, fileInputStream
                                )
                            } else if (galleryItem.attachmentType == Constants.AttachmentType.VIDEO.type) {
                                extension = "mp4"
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
                                status = Constants.AttachmentStatus.SENDING.status,
                                duration = 0L,
                                extension = extension
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
                    with(galleryShareViewModel) {
                        galleryItem.contentUri?.let { uri ->
                            setImageUriSelected(uri)
                            resetUriImageSelected()
                        }
                    }
                    when (location) {
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

    //region Implementation LoaderManager.LoaderCallbacks<Cursor>
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val projectionFilesFolder = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        val isConversation =
            this.args.location == Constants.LocationImageSelectorBottomSheet.CONVERSATION.location

        //WHERE
        var selection = if (isConversation) {
            "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?) " +
                    "AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml'"
        } else {
            "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} <> ? AND ${MediaStore.Files.FileColumns.MIME_TYPE} <> 'image/svg+xml'"
        }

        selection = "$selection AND ${MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME}=?"

        //WHERE ARGS
        val selectionArgs: Array<String> = if (isConversation) {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(),
                this.args.galleryFolder.folderName
            )
        } else {
            arrayOf(
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                MediaStore.Files.FileColumns.MEDIA_TYPE_NONE.toString(),
                this.args.galleryFolder.folderName
            )
        }

        return CursorLoader(
            requireContext(),
            MediaStore.Files.getContentUri("external"),
            projectionFilesFolder,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC, ${MediaStore.Files.FileColumns._ID} DESC"
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        adapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.swapCursor(null)
    }
    //endregion
}
