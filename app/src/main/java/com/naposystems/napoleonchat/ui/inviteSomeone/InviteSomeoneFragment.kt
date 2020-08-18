package com.naposystems.napoleonchat.ui.inviteSomeone

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.InviteSomeoneFragmentBinding

class InviteSomeoneFragment : Fragment() {

    companion object {
        fun newInstance() = InviteSomeoneFragment()
    }

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

}
