package com.naposystems.pepito.ui.inviteSomeone

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.InviteSomeoneFragmentBinding

class InviteSomeoneFragment : Fragment() {

    companion object {
        fun newInstance() = InviteSomeoneFragment()
    }

    private lateinit var viewModel: InviteSomeoneViewModel
    private lateinit var binding: InviteSomeoneFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.invite_someone_fragment, container, false
        )

        binding.buttonShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.text_share_link))
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(InviteSomeoneViewModel::class.java)
    }

}
