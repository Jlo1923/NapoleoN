package com.naposystems.napoleonchat.ui.attachmentDocument.adapter

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cursoradapter.widget.CursorAdapter
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.AttachmentDocumentItemBinding
import com.naposystems.napoleonchat.utility.Utils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AttachmentDocumentAdapter constructor(context: Context?, cursor: Cursor?) :
    CursorAdapter(context, cursor, 0) {

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        val layoutInflater = LayoutInflater.from(context)
        val binding: AttachmentDocumentItemBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.attachment_document_item,
            parent,
            false
        )

        return binding.root
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        view?.let {
            DataBindingUtil.getBinding<AttachmentDocumentItemBinding>(view)?.let { binding ->

                cursor?.let { cursor ->

                    val displayNameIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)
                    val displayName = cursor.getString(displayNameIndex)

                    val sizeIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val size = cursor.getLong(sizeIndex)

                    val dateIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                    val date = cursor.getLong(dateIndex)

                    val dataIndex =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    val data = cursor.getString(dataIndex)

                    val file = File(data)
                    val extension = file.extension

                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val netDate = Date(date * 1000)

                    binding.textViewDocumentName.text = "$displayName.$extension"
                    binding.textViewDocumentSize.text = Utils.getFileSize(size)
                    binding.textViewDocumentDate.text = sdf.format(netDate)
                    binding.textViewDocumentExtension.apply {
                        text = extension
                        tag = extension
                    }
                }
            }
        }
    }
}