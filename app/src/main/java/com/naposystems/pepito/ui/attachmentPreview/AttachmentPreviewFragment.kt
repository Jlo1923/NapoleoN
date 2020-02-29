package com.naposystems.pepito.ui.attachmentPreview

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentPreviewFragmentBinding
import com.naposystems.pepito.utility.Utils
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class AttachmentPreviewFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentPreviewFragment()
    }

    private val viewModel: AttachmentPreviewViewModel by viewModels()
    private val conversationShareViewModel: ConversationShareViewModel by activityViewModels()

    private lateinit var binding: AttachmentPreviewFragmentBinding
    private val args: AttachmentPreviewFragmentArgs by navArgs()

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

        binding.galleryItem = args.galleryItem
        binding.executePendingBindings()

        if (args.galleryItem.contentUri != null) {
            conversationShareViewModel.setImageUri(args.galleryItem.contentUri!!.path!!)

            GlobalScope.launch {
                val fileDescriptor = context!!.contentResolver
                    .openAssetFileDescriptor(args.galleryItem.contentUri!!, "r")
                val fileInputStream = FileInputStream(fileDescriptor!!.fileDescriptor)

                conversationShareViewModel.setImageBase64(
                    Utils.convertFileInputStreamToBase64(
                        fileInputStream
                    )
                )
            }
        }

        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            conversationShareViewModel.setMessage(binding.inputPanel.getEditTex().text.toString())
            conversationShareViewModel.setGallerySendClicked()
            conversationShareViewModel.resetGallerySendClicked()
            conversationShareViewModel.resetMessage()
            this.findNavController().popBackStack(R.id.conversationFragment, false)
            /*this.findNavController().navigate(
                AttachmentPreviewFragmentDirections.actionAttachmentPreviewFragmentToConversationFragment(
                    args.contact
                )
            )*/
        }

        binding.imageButtonClose.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}
