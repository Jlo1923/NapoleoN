package com.naposystems.pepito.ui.napoleonKeyboardStickerPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.storage.FirebaseStorage
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.NapoleonKeyboardStickerPageFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.model.napoleonEmoji.NapoleonEmojiRemoteConfig
import com.naposystems.pepito.ui.napoleonKeyboardSticker.NapoleonKeyboardStickerFragment
import com.naposystems.pepito.ui.napoleonKeyboardStickerPage.adapter.NapoleonKeyboardStickerPageAdapter
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import java.io.File

class NapoleonKeyboardStickerPageFragment : Fragment() {

    private var napoleonEmojiRemoteConfig: NapoleonEmojiRemoteConfig? = null
    private lateinit var binding: NapoleonKeyboardStickerPageFragmentBinding
    private val shareViewModel: ConversationShareViewModel by activityViewModels()

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
                            val attachment = Attachment(
                                id = 0,
                                messageId = 0,
                                webId = "",
                                messageWebId = "",
                                type = Constants.AttachmentType.GIF_NN.type,
                                body = "",
                                uri = file.name,
                                origin = Constants.AttachmentOrigin.DOWNLOADED.origin,
                                thumbnailUri = "",
                                status = Constants.AttachmentStatus.SENDING.status,
                                extension = "gif"
                            )

                            with(shareViewModel) {
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
