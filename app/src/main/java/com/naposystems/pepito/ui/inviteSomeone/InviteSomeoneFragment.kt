package com.naposystems.pepito.ui.inviteSomeone

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.naposystems.pepito.R

class InviteSomeoneFragment : Fragment() {

    companion object {
        fun newInstance() = InviteSomeoneFragment()
    }

    private lateinit var viewModel: InviteSomeoneViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.invite_someone_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InviteSomeoneViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
