package com.naposystems.napoleonchat.ui.customUserNotification

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomUserNotificationFragmentBinding

class CustomUserNotificationFragment : Fragment() {

    companion object {
        fun newInstance() = CustomUserNotificationFragment()
    }

    private lateinit var viewModel: CustomUserNotificationViewModel
    private lateinit var binding: CustomUserNotificationFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.custom_user_notification_fragment,
            container,
            false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun enableOptions() {

    }

    private fun disableOptions() {

    }
}