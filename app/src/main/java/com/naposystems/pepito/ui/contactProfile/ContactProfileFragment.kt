package com.naposystems.pepito.ui.contactProfile

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ContactProfileFragmentBinding
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber
import javax.inject.Inject

class ContactProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ContactProfileFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactProfileViewModel
    private val args: ContactProfileFragmentArgs by navArgs()
    private lateinit var binding: ContactProfileFragmentBinding

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.contact_profile_fragment, container, false
        )

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ContactProfileViewModel::class.java)

        binding.viewmodel = viewModel

        viewModel.getLocalContact(args.contactId)

    }

}
