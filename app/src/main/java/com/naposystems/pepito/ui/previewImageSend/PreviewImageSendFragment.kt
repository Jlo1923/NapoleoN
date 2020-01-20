package com.naposystems.pepito.ui.previewImageSend

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.PreviewImageSendFragmentBinding
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class PreviewImageSendFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewImageSendFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private lateinit var viewModel: SharePreviewImageSendViewModel
    private lateinit var binding: PreviewImageSendFragmentBinding
    private lateinit var bitMap: Bitmap
    private val args: PreviewImageSendFragmentArgs by navArgs()

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
            R.layout.preview_image_send_fragment,
            container,
            false
        )

        bitMap = args.bitmap

        binding.imageViewImageSend.setImageBitmap(bitMap)

        binding.imageButtonClose.setOnClickListener {
            viewModel.setCancelClicked()
            viewModel.resetCancelClicked()
            this.findNavController().popBackStack()
        }

        binding.inputPanel.getFloatingActionButton().setOnClickListener {
            viewModel.setMessage(binding.inputPanel.getEditTex().text.toString())
            viewModel.resetMessage()
            this.findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProviders.of(activity!!, viewModelFactory)
                .get(SharePreviewImageSendViewModel::class.java)
    }

}
