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
import com.naposystems.napoleonchat.utility.Utils.Companion.setSafeOnClickListener

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

        binding.buttonShareAndroid.setSafeOnClickListener {
            shareLink(R.string.text_share_link_android)
        }

        binding.buttonShareIos.setSafeOnClickListener {
            shareLink(R.string.text_share_link_ios)
        }

        return binding.root
    }

    private fun shareLink(string : Int) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(string))
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

}
