package com.naposystems.napoleonchat.ui.napoleonKeyboardStickerPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.storage.FirebaseStorage
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardStickerPageFragmentBinding
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.model.napoleonEmoji.NapoleonEmojiRemoteConfig
import com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.napoleonchat.ui.napoleonKeyboardStickerPage.adapter.NapoleonKeyboardStickerPageAdapter
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.sharedViewModels.conversation.ConversationSharedViewModel
import java.io.File

class NapoleonKeyboardStickerPageFragment : Fragment() {

    private var napoleonEmojiRemoteConfig: NapoleonEmojiRemoteConfig? = null
    private lateinit var binding: NapoleonKeyboardStickerPageFragmentBinding
    private val sharedViewModel: ConversationSharedViewModel by activityViewModels()

    private lateinit var mFirebaseStorage: FirebaseStorage
    private var mListener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener? = null

    companion object {
        const val NAPOLEON_EMOJI_KEY = "napoleon_emoji"

        fun newInstance(napoleonEmoji: NapoleonEmojiRemoteConfig) =
            NapoleonKeyboardStickerPageFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(NAPOLEON_EMOJI_KEY, napoleonEmoji)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mFirebaseStorage = FirebaseStorage.getInstance()

        arguments?.let { bundle ->
            if (bundle.containsKey(NAPOLEON_EMOJI_KEY)) {
                napoleonEmojiRemoteConfig =
                    bundle.getSerializable(NAPOLEON_EMOJI_KEY) as NapoleonEmojiRemoteConfig
            }
        }

        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.napoleon_keyboard_sticker_page_fragment,
            container,
            false
        )

        val storageRef = mFirebaseStorage.reference

        napoleonEmojiRemoteConfig?.let { napoleonEmojiRemoteConfig ->
            val reference = storageRef.child(napoleonEmojiRemoteConfig.type)
            reference.listAll()
                .addOnSuccessListener { listResult ->
                    val adapter = NapoleonKeyboardStickerPageAdapter(object :
                        NapoleonKeyboardStickerPageAdapter.NapoleonKeyboardStickerPageListener {
                        override fun onGifSelected(file: File) {
                            val attachment = AttachmentEntity(
                                id = 0,
                                messageId = 0,
                                webId = "",
                                messageWebId = "",
                                type = Constants.AttachmentType.GIF_NN.type,
                                body = "",
                                fileName = file.name,
                                origin = Constants.AttachmentOrigin.DOWNLOADED.origin,
                                thumbnailUri = "",
                                status = Constants.AttachmentStatus.SENDING.status,
                                extension = "gif",
                                duration = 0L
                            )

                            with(sharedViewModel) {
                                setMessage("")
                                setAttachmentSelected(attachment)
                                resetAttachmentSelected()
                                resetMessage()
                            }
                            mListener?.onStickerSelected()
                        }
                    })

                    adapter.submitList(listResult.items)

                    binding.recyclerViewNapoleonGif.adapter = adapter
                }
        }

        return binding.root
    }

    fun setListener(listener: NapoleonKeyboardStickerFragment.NapoleonKeyboardStickerListener?) {
        this.mListener = listener
    }

}
