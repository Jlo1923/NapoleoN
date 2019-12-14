package com.naposystems.pepito.ui.contacts

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.ContactsFragmentBinding
import com.naposystems.pepito.ui.contacts.adapter.ContactsAdapter
import com.naposystems.pepito.utility.viewModel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class ContactsFragment : Fragment() {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: ContactsViewModel
    private lateinit var binding: ContactsFragmentBinding
    private lateinit var adapter: ContactsAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.contacts_fragment, container, false
        )

        setHasOptionsMenu(true)

        binding.lifecycleOwner = this

        setAdapter()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(ContactsViewModel::class.java)

        binding.viewModel = viewModel

        viewModel.getContacts()

        viewModel.contacts.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contacts, menu)

        val searchManager = context!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            setIconifiedByDefault(false)
        }
    }

    private fun setAdapter() {
        adapter = ContactsAdapter(ContactsAdapter.ContactClickListener {
            Toast.makeText(context!!, "Has seleccionado ${it.nickname}", Toast.LENGTH_SHORT).show()
        })

        binding.recyclerViewContacts.adapter = adapter
    }

}
