package com.naposystems.pepito.ui.previewImage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs
import androidx.transition.ChangeBounds
import com.bumptech.glide.Glide

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.PreviewImageFragmentBinding
import com.naposystems.pepito.ui.mainActivity.MainActivity

class PreviewImageFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewImageFragment()
    }

    private lateinit var binding: PreviewImageFragmentBinding
    private val args: PreviewImageFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(android.R.transition.move)*/
        sharedElementEnterTransition = ChangeBounds().apply { duration = 350 }
        sharedElementReturnTransition = ChangeBounds().apply { duration = 350 }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.preview_image_fragment, container, false
        )

        Glide.with(context!!)
            .load(args.imageUrl)
            .into(binding.imageViewPreview)

        if (!args.titleToolbar.isNullOrEmpty()){
            (activity as MainActivity).supportActionBar?.title = args.titleToolbar
        }

        return binding.root
    }


}
