package com.naposystems.napoleonchat.ui.previewBackgroundChat

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
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.PreviewBackgroundChatFragmentBinding
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.FileManager
import com.naposystems.napoleonchat.utility.Utils
import com.naposystems.napoleonchat.utility.adapters.showToast
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class PreviewBackgroundChatFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewBackgroundChatFragment()
    }

    private val args: PreviewBackgroundChatFragmentArgs by navArgs()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PreviewBackgroundChatViewModel by viewModels { viewModelFactory }
    private lateinit var binding: PreviewBackgroundChatFragmentBinding
    private val fileName: String = "chat_background.jpg"

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.preview_background_chat_fragment,
            container,
            false
        )



        binding.buttonAccept.setOnClickListener {
            context?.let { context ->
                val fileUri = Utils.getFileUri(
                    context = context,
                    fileName = args.uri,
                    subFolder = Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder
                )

                val nullableInputStream = context.contentResolver.openInputStream(fileUri)

                nullableInputStream?.let { inputStream ->

                    GlobalScope.launch {
                        val file = FileManager.copyFile(
                            context,
                            inputStream,
                            Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder,
                            fileName
                        )

                        viewModel.updateChatBackground(file.name)
                        clearCache(context)
                    }
                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            viewModel.resetChatBackground()
            findNavController().popBackStack()
        }

        binding.lifecycleOwner = this

        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.setChatBackground(args.uri)

        viewModel.chatBackgroundUpdated.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                this.showToast(getString(R.string.text_updated_successfully))
                viewModel.resetChatBackground()
                viewModel.resetChatBackgroundUpdated()
                findNavController().popBackStack()
            } else if (it == false) {
                Utils.showSimpleSnackbar(
                    binding.coordinator,
                    getString(R.string.text_error_updating_conversation_background),
                    3
                )
                viewModel.resetChatBackgroundUpdated()
            }
        })

        viewModel.chatBackground.observe(viewLifecycleOwner, Observer { uriString ->
            if (!uriString.isNullOrEmpty()) {
                context?.let { context ->
                    val uri = Utils.getFileUri(
                        context = context,
                        fileName = args.uri,
                        subFolder = Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder
                    )
                    Glide.with(binding.imageViewBackground)
                        .load(uri)
                        .centerCrop()
                        .into(binding.imageViewBackground)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetChatBackground()
        viewModel.resetChatBackgroundUpdated()
    }

    private fun clearCache(context: Context) {
        val path = File(context.cacheDir!!.absolutePath, Constants.NapoleonCacheDirectories.CHAT_BACKGROUND.folder)
        if (path.exists() && path.isDirectory) {
            for (child in path.listFiles()!!) {
                if (child.name != fileName) {
                    child.delete()
                }
            }
        }
    }
}
