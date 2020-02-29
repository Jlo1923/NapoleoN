package com.naposystems.pepito.ui.attachmentGallery

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentGalleryFragmentBinding
import com.naposystems.pepito.model.attachment.gallery.GalleryItem
import com.naposystems.pepito.ui.attachmentGallery.adapter.AttachmentGalleryAdapter
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AttachmentGalleryFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentGalleryFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: AttachmentGalleryViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: AttachmentGalleryFragmentBinding
    private lateinit var adapter: AttachmentGalleryAdapter
    private val args: AttachmentGalleryFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_gallery_fragment, container, false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        postponeEnterTransition()
        binding.recyclerViewGalleryItems.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.loadGalleryItemsByFolder(args.folderName)

        viewModel.galleryItems.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                adapter.submitList(it)
            }
        })
    }

    private fun setupAdapter() {
        adapter = AttachmentGalleryAdapter(object : AttachmentGalleryAdapter.ClickListener {
            override fun onClick(galleryItem: GalleryItem, imageView: ImageView) {
                val extras = FragmentNavigatorExtras(
                    imageView to imageView.transitionName
                )

                this@AttachmentGalleryFragment.findNavController().navigate(
                    AttachmentGalleryFragmentDirections.actionAttachmentGalleryFragmentToAttachmentPreviewFragment(
                        args.contact,
                        galleryItem
                    ),
                    extras
                )
            }
        })

        binding.recyclerViewGalleryItems.adapter = adapter
    }

}
