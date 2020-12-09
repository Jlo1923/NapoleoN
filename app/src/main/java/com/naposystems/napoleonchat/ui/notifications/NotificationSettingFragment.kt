package com.naposystems.napoleonchat.ui.notifications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NotificationFragmentBinding
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import dagger.android.support.AndroidSupportInjection
import timber.log.Timber


class NotificationSettingFragment : BaseFragment() {

    companion object {
        fun newInstance() = NotificationSettingFragment()
        const val RINGTONE_NOTIFICATION_CODE = 9
    }

    private lateinit var viewModel: NotificationSettingViewModel
    private lateinit var binding: NotificationFragmentBinding
    private lateinit var notificationUtils: NotificationUtils
    private var currentSoundNotificationMessage: Uri? = null
    private var soundTitle = ""

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.notification_fragment, container, false)

        notificationUtils = activity?.let { NotificationUtils(it.applicationContext) }!!

        binding.optionNotificationTone.setOnClickListener {
            openNotificationTone()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateSoundChannelMessage()
    }

    private fun updateSoundChannelMessage() {
        currentSoundNotificationMessage = notificationUtils.getChannelSound(requireContext())

        soundTitle =
            RingtoneManager.getRingtone(context, currentSoundNotificationMessage)
                .getTitle(requireContext())

        binding.textViewSoundNotification.text = soundTitle
    }

    private fun openNotificationTone() {
        validateStateOutputControl()
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Seleccione tono de notificación|!!")
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
            currentSoundNotificationMessage
        )
        this.startActivityForResult(intent, RINGTONE_NOTIFICATION_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == RINGTONE_NOTIFICATION_CODE) {
            val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            notificationUtils.changeChannel(requireContext(), uri)
            Timber.d("*TestSong: onActivityResult=$uri")

            updateSoundChannelMessage()
        }
    }
}