package com.naposystems.pepito.ui.attachmentDocument

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ListView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.ListFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentDocumentFragmentBinding
import com.naposystems.pepito.databinding.AttachmentDocumentItemBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.ui.attachmentDocument.adapter.AttachmentDocumentAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.FileManager
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import kotlinx.coroutines.launch
import java.io.FileInputStream


class AttachmentDocumentFragment : ListFragment(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        fun newInstance() = AttachmentDocumentFragment()
    }

    private lateinit var mAdapter: AttachmentDocumentAdapter
    private val viewModel: AttachmentDocumentViewModel by viewModels()
    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()
    private lateinit var binding: AttachmentDocumentFragmentBinding

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setEmptyText("No tienes documentos!!")

        mAdapter = AttachmentDocumentAdapter(context, null)

        setListShown(false)

        listView.divider = null
        listView.dividerHeight = 0

        listAdapter = mAdapter

        LoaderManager.getInstance(this).initLoader(0, null, this)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        lifecycleScope.launch {

            DataBindingUtil.getBinding<AttachmentDocumentItemBinding>(v)?.let { binding ->

                val extension = binding.textViewDocumentExtension.tag as String

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Files.getContentUri("external"),
                    id
                )

                val parcelFileDescriptor =
                    context!!.contentResolver.openFileDescriptor(contentUri, "r")

                parcelFileDescriptor?.let {

                    val fileDescriptor = it.fileDescriptor
                    val fileInputStream = FileInputStream(fileDescriptor)

                    val fileSelected = FileManager.copyFile(
                        context!!,
                        fileInputStream,
                        Constants.NapoleonCacheDirectories.DOCUMENTOS.folder,
                        "${System.currentTimeMillis()}.$extension"
                    )

                    val attachment = Attachment(
                        id = 0,
                        messageId = 0,
                        webId = "",
                        messageWebId = "",
                        type = Constants.AttachmentType.DOCUMENT.type,
                        body = "",
                        uri = fileSelected.name,
                        origin = Constants.AttachmentOrigin.GALLERY.origin,
                        thumbnailUri = "",
                        status = Constants.AttachmentStatus.SENDING.status,
                        extension = extension
                    )

                    with(conversationShareViewModel) {
                        setMessage("")
                        setAttachmentSelected(attachment)
                        resetAttachmentSelected()
                        resetMessage()
                    }
                    findNavController().popBackStack(R.id.conversationFragment, false)
                }
            }
        }
    }

    //region Implementation LoaderManager.LoaderCallbacks<Cursor>
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {

        val projectionDocuments = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.TITLE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATA

        )

        val selectionDocuments = "${MediaStore.Files.FileColumns.SIZE}>0 AND " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.DATA} LIKE ? "

        val selectionArgsDocuments = arrayOf(
            "%pdf",
            "%doc",
            "%docx",
            "%xls",
            "%xlsx",
            "%ppt",
            "%pptx"
        )

        return (activity as? Context)?.let { context ->
            CursorLoader(
                context,
                MediaStore.Files.getContentUri("external"),
                projectionDocuments,
                selectionDocuments,
                selectionArgsDocuments,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )
        } ?: throw Exception("Activity cannot be null")
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        mAdapter.swapCursor(data)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mAdapter.swapCursor(null)
    }
//endregion
}
