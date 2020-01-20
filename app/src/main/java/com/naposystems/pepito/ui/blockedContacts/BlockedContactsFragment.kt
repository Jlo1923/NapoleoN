package com.naposystems.pepito.ui.blockedContacts

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.BlockedContactsFragmentBinding
import com.naposystems.pepito.ui.blockedContacts.adapter.BlockedContactsAdapter
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BlockedContactsFragment : Fragment() {

    companion object {
        fun newInstance() = BlockedContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: BlockedContactsViewModel
    private lateinit var binding: BlockedContactsFragmentBinding
    private lateinit var adapter: BlockedContactsAdapter


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.blocked_contacts_fragment, container, false
        )

        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(BlockedContactsViewModel::class.java)

        viewModel.blockedContacts.observe(viewLifecycleOwner, Observer { blockedContacts ->
            adapter = BlockedContactsAdapter(
                blockedContacts,
                BlockedContactsAdapter.BlockedContactSelectionListener {
                    Toast.makeText(context!!, it.nickname, Toast.LENGTH_SHORT).show()
                })

            binding.recyclerViewBlockedContacts.adapter = adapter

            binding.viewSwitcher.showNext()
        })
    }

}
