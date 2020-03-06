package com.naposystems.pepito.ui.attachmentGalleryFolder

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
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.AttachmentGalleryFoldersFragmentBinding
import com.naposystems.pepito.model.attachment.gallery.GalleryFolder
import com.naposystems.pepito.ui.attachmentGalleryFolder.adapter.AttachmentGalleryFolderAdapter
import com.naposystems.pepito.utility.adapters.showToast
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class AttachmentGalleryFoldersFragment : Fragment() {

    companion object {
        fun newInstance() = AttachmentGalleryFoldersFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: AttachmentGalleryFoldersViewModel by viewModels {
        viewModelFactory
    }
    private val args: AttachmentGalleryFoldersFragmentArgs by navArgs()
    private lateinit var binding: AttachmentGalleryFoldersFragmentBinding
    private lateinit var adapter: AttachmentGalleryFolderAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.attachment_gallery_folders_fragment, container, false
        )

        setupAdapter()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loadFolders()

        viewModel.folders.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun setupAdapter() {
        adapter =
            AttachmentGalleryFolderAdapter(object : AttachmentGalleryFolderAdapter.ClickListener {
                override fun onClick(galleryFolder: GalleryFolder) {
                    this@AttachmentGalleryFoldersFragment.showToast(galleryFolder.folderName)
                    findNavController().navigate(
                        AttachmentGalleryFoldersFragmentDirections.actionAttachmentGalleryFragmentToAttachmentGalleryFragment(
                            args.contact,
                            galleryFolder
                        )
                    )
                }
            })

        binding.recyclerViewFolders.adapter = adapter
    }

}
