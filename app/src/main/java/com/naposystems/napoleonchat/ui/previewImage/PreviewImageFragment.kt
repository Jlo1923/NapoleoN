package com.naposystems.napoleonchat.ui.previewImage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.transition.ChangeBounds
import com.bumptech.glide.Glide
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.PreviewImageFragmentBinding
import com.naposystems.napoleonchat.ui.mainActivity.MainActivity
import com.naposystems.napoleonchat.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class PreviewImageFragment : Fragment() {

    companion object {
        fun newInstance() = PreviewImageFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: PreviewImageViewModel by viewModels { viewModelFactory }
    private lateinit var binding: PreviewImageFragmentBinding
    private val args: PreviewImageFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = ChangeBounds().apply { duration = 350 }
        sharedElementReturnTransition = ChangeBounds().apply { duration = 350 }
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
            inflater, R.layout.preview_image_fragment, container, false
        )

        if (!args.titleToolbar.isNullOrEmpty()){
            (activity as MainActivity).supportActionBar?.title = args.titleToolbar
        }

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetImage()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        context?.let {context ->
            args.contact?.let {contact ->
                viewModel.setContact(context, contact)
            }

            args.user?.let {user ->
                viewModel.setUser(context, user)
            }

            viewModel.image.observe(viewLifecycleOwner, Observer { image ->
                if (image != null) {
                    Glide.with(context)
                        .load(image)
                        .into(binding.imageViewPreview)
                } else {
                    binding.imageViewPreview.setImageDrawable(
                        context.resources.getDrawable(R.drawable.ic_default_avatar, context.theme)
                    )
                }
            })
        }
    }
}
