package com.naposystems.pepito.ui.napoleonKeyboardGif

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.pagination.GPHContent
import com.giphy.sdk.ui.views.GPHGridCallback
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.NapoleonKeyboardGifFragmentBinding
import com.naposystems.pepito.entity.message.attachments.Attachment
import com.naposystems.pepito.utility.Constants
import com.naposystems.pepito.utility.DownloadFileResult
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.sharedViewModels.conversation.ConversationShareViewModel
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class NapoleonKeyboardGifFragment : Fragment() {

    companion object {
        fun newInstance() = NapoleonKeyboardGifFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: NapoleonKeyboardGifFragmentBinding
    private var mListener: NapoleonKeyboardGifListener? = null
    private val viewModel: NapoleonKeyboardGifViewModel by viewModels { viewModelFactory }
    private val shareViewModel: ConversationShareViewModel by activityViewModels()

    interface NapoleonKeyboardGifListener {
        fun onSearchFocused()
        fun onGifSelected()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.napoleon_keyboard_gif_fragment, container, false
        )

        binding.giphySearchBar.hideKeyboardOnSearch = false

        setGiphySearchBarListeners()

        binding.giphyGrid.callback = object : GPHGridCallback {
            override fun contentDidUpdate(resultCount: Int) {
                // Intentionally empty
            }

            override fun didSelectMedia(media: Media) {
                Timber.d("Url: ${media.images.original?.gifUrl}")

                if (binding.viewSwitcher.nextView.id == binding.containerProgress.id) {
                    binding.viewSwitcher.showNext()
                    viewModel.downloadGif(media)
                }
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.downloadAttachmentProgress.observe(viewLifecycleOwner, Observer {
            when (it) {
                is DownloadFileResult.Success -> {
                    if (binding.viewSwitcher.nextView.id == binding.containerGiphy.id) {
                        binding.viewSwitcher.showNext()
                    }
                    val attachment = Attachment(
                        id = 0,
                        messageId = 0,
                        webId = "",
                        messageWebId = "",
                        type = Constants.AttachmentType.GIF.type,
                        body = "",
                        uri = it.fileName,
                        origin = Constants.AttachmentOrigin.DOWNLOADED.origin,
                        thumbnailUri = "",
                        status = Constants.AttachmentStatus.SENDING.status,
                        extension = "gif"
                    )

                    shareViewModel.setGifSelected(attachment)
                    mListener?.onGifSelected()
                }
                is DownloadFileResult.Progress -> {
                    binding.textViewProgress.text = "${it.progress}%"
                    binding.progressBar.setProgress(it.progress.toFloat())
                }
                is DownloadFileResult.Error -> {
                    this.showToast(getString(R.string.text_error_sending_gif))
                    if (binding.viewSwitcher.nextView.id == binding.containerGiphy.id) {
                        binding.viewSwitcher.showNext()
                    }
                }
            }

        })
    }

    private fun setGiphySearchBarListeners() {
        var giphyEditText: AppCompatEditText? = null

        binding.giphySearchBar.children.forEach { child ->
            if (child is AppCompatEditText) {
                giphyEditText = child
            }
        }

        binding.giphySearchBar.hideKeyboardOnSearch = true

        giphyEditText?.let { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.requestFocus()
                    mListener?.onSearchFocused()
                }
            }

            editText.setOnClickListener {
                editText.isFocusable = true
                editText.isFocusableInTouchMode = true
                editText.requestFocus()
                mListener?.onSearchFocused()
            }

            editText.doOnTextChanged { text, _, _, _ ->
                text?.let { charSequence ->
                    if (charSequence.isEmpty()) {
                        binding.giphyGrid.content = GPHContent.trendingGifs
                    } else {
                        binding.giphyGrid.content =
                            GPHContent.searchQuery(charSequence.toString())
                    }
                }
            }
        }

        binding.giphyGrid.content = GPHContent.trendingGifs
    }

    fun setListener(listener: NapoleonKeyboardGifListener) {
        this.mListener = listener
    }

}
