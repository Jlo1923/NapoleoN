package com.naposystems.napoleonchat.ui.napoleonKeyboardSticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardStickerFragmentBinding
import com.naposystems.napoleonchat.databinding.NapoleonKeyboardStickerHeaderItemBinding
import com.naposystems.napoleonchat.model.napoleonEmoji.NapoleonEmojiRemoteConfig
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.ui.napoleonKeyboardSticker.adapter.NapoleonKeyboardStickerPagerAdapter
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.REMOTE_CONFIG_EMOJIS_KEY
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.json.JSONException
import timber.log.Timber

class NapoleonKeyboardStickerFragment : Fragment() {

    companion object {
        fun newInstance() = NapoleonKeyboardStickerFragment()
    }

    private lateinit var binding: NapoleonKeyboardStickerFragmentBinding
    private lateinit var mFirebaseRemoteConfig: FirebaseRemoteConfig
    private lateinit var mFirebaseStorage: FirebaseStorage
    private var mListener: NapoleonKeyboardStickerListener? = null

    private val listNapoleonEmoji by lazy {
        mutableListOf<StorageReference>()
    }

    interface NapoleonKeyboardStickerListener {
        fun onStickerSelected()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.napoleon_keyboard_sticker_fragment, container, false
        )

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseStorage = FirebaseStorage.getInstance()

        getRemoteConfig()

        return binding.root
    }

    private fun getRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getJsonEmojis()
                } else {
                    this.showToast("No se han podido obtener el remote config")
                }
            }
    }

    private fun getJsonEmojis() {
        val jsonString = mFirebaseRemoteConfig.getString(REMOTE_CONFIG_EMOJIS_KEY)

        try {

            val moshi = Moshi.Builder().build()

            val listType =
                Types.newParameterizedType(List::class.java, NapoleonEmojiRemoteConfig::class.java)
            val adapter: JsonAdapter<List<NapoleonEmojiRemoteConfig>> = moshi.adapter(listType)

            val napoleonEmojiList = adapter.fromJson(jsonString)

            downloadEmojiFiles(napoleonEmojiList)

        } catch (e: JSONException) {
            Timber.e(e)
        }
    }

    private fun downloadEmojiFiles(napoleonEmojiRemoteConfigList: List<NapoleonEmojiRemoteConfig>?) {

        napoleonEmojiRemoteConfigList?.let { emojiList ->
            val storageRef = mFirebaseStorage.reference

            inflateHeader(storageRef, napoleonEmojiRemoteConfigList)

            emojiList.forEach { napoleonEmoji ->
                listNapoleonEmoji.add(storageRef.child(napoleonEmoji.type))
            }

            context?.let { secureContext ->
                val viewPagerAdapter = NapoleonKeyboardStickerPagerAdapter(
                    secureContext as MainActivity,
                    emojiList,
                    mListener
                )

                binding.viewPager.adapter = viewPagerAdapter
            }
        }
    }

    private fun inflateHeader(
        storageRef: StorageReference,
        napoleonEmojiRemoteConfigList: List<NapoleonEmojiRemoteConfig>
    ) {
        try {
            val emojisRef = storageRef.child("emojis")

            napoleonEmojiRemoteConfigList.forEachIndexed { index, napoleonEmoji ->
                context?.let {
                    val itemStorageRef = emojisRef.child("${napoleonEmoji.type}.png")

                    val layoutInflater = LayoutInflater.from(requireContext())
                    val bindingHeaderItem =
                        NapoleonKeyboardStickerHeaderItemBinding.inflate(layoutInflater)

                    val file = FileManager.createFile(
                        it,
                        itemStorageRef.name,
                        Constants.CacheDirectories.IMAGES.folder
                    )

                    if (!file.exists()) {
                        itemStorageRef.getFile(file)
                            .addOnSuccessListener {
                                bindingHeaderItem.imageViewProgress.loadImageFile(file)
                            }
                    } else {
                        bindingHeaderItem.imageViewProgress.loadImageFile(file)
                    }

                    binding.listViewHeader.addView(bindingHeaderItem.root)

                    val layoutParams = bindingHeaderItem.imageViewProgress.layoutParams
                    layoutParams.apply {
                        val dp80 = Utils.dpToPx(requireContext(), 60f)
                        width = dp80
                        height = dp80
                    }

                    bindingHeaderItem.root.setOnClickListener(headerItemClickListener(index))

                    bindingHeaderItem.imageViewProgress.layoutParams = layoutParams
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    private fun headerItemClickListener(position: Int) = View.OnClickListener {
        binding.viewPager.setCurrentItem(position, true)
    }

    fun setListener(listener: NapoleonKeyboardStickerListener) {
        this.mListener = listener
    }
}
