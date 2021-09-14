package com.naposystems.napoleonchat.ui.home.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.naposystems.napoleonchat.ui.addContact.AddContactFragment
import com.naposystems.napoleonchat.ui.contacts.ContactsFragment
import com.naposystems.napoleonchat.ui.home.TabsPagerFragmentDirections
import com.naposystems.napoleonchat.utility.Constants

class TabsPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle, private var numberOfTabs: Int) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                // # Contacts Fragment
                return ContactsFragment()
            }
            1 -> {
                // # solictudes Fragment
                //location = Constants.LocationAddContact.HOME.location
                return AddContactFragment()
            }
            2 -> {
                    TabsPagerFragmentDirections.actionContactsFragmentToAddContactFragment(
                        location = Constants.LocationAddContact.HOME.location
                    )
                return AddContactFragment()
            }
            else -> return ContactsFragment()
        }
    }

    override fun getItemCount(): Int {
        return numberOfTabs
    }
}