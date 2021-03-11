package com.naposystems.napoleonchat.ui.notificationSetting

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.NotificationSettingFragmentBinding
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.service.notification.NotificationService
import timber.log.Timber
import javax.inject.Inject


class NotificationSettingFragment : BaseFragment() {

    companion object {
        fun newInstance() = NotificationSettingFragment()
        const val RINGTONE_NOTIFICATION_CODE = 9
    }


    private lateinit var viewModel: NotificationSettingViewModel
    private lateinit var binding: NotificationSettingFragmentBinding

    @Inject
    lateinit var notificationService: NotificationService

    private var currentSoundNotificationMessage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.notification_setting_fragment,
                container,
                false
            )

        binding.optionNotificationTone.setOnClickListener { openNotificationTone() }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        updateSoundChannelMessage()
    }

    private fun updateSoundChannelMessage() {
//        notificationService = NotificationService()
//        notificationService = NotificationService(requireContext().applicationContext)
        currentSoundNotificationMessage = notificationService.getChannelSound(
            requireContext(),
            Constants.ChannelType.DEFAULT.type,
            null,
            null
        )

        val soundTitle =
            RingtoneManager.getRingtone(context, currentSoundNotificationMessage)
                .getTitle(requireContext())

        binding.textViewSoundNotification.text = soundTitle
    }

    private fun openNotificationTone() {
        validateStateOutputControl()
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        intent.putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            getString(R.string.text_select_notification_tone)
        )
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

            notificationService.updateChannel(
                requireContext(),
                uri,
                Constants.ChannelType.DEFAULT.type
            )
            Timber.d("*TestSong: onActivityResult=$uri")

            updateSoundChannelMessage()
        }
    }
}