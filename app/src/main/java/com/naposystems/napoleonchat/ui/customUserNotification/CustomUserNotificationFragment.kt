package com.naposystems.napoleonchat.ui.customUserNotification

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.CustomUserNotificationFragmentBinding
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utils.handlerNotificationChannel.HandlerNotificationChannel
import com.naposystems.napoleonchat.ui.baseFragment.BaseFragment
import com.naposystems.napoleonchat.utility.Constants
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class CustomUserNotificationFragment : BaseFragment() {

    companion object {
        fun newInstance() = CustomUserNotificationFragment()
        const val RINGTONE_NOTIFICATION_CODE = 9
    }

    //TODO: Revisar funcionamiento este viewModel
    private lateinit var viewModel: CustomUserNotificationViewModel

    private lateinit var binding: CustomUserNotificationFragmentBinding

    @Inject
    lateinit var handlerNotificationChannel: HandlerNotificationChannel

    private var currentSoundNotificationMessage: Uri? = null

    private val args: CustomUserNotificationFragmentArgs by navArgs()

    private val disposable: CompositeDisposable by lazy {
        CompositeDisposable()
    }

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

        val disposableContactBlockOrDelete =
            RxBus.listen(RxEvent.ContactBlockOrDelete::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { eventContact ->
                    if (args.contact.id == eventContact.contactId) {
                        if (args.contact.stateNotification) {
                            handlerNotificationChannel.deleteUserChannel(
                                args.contact.id,
                                args.contact.getNickName()
                            )
                        }
                        findNavController().popBackStack(R.id.homeFragment, false)
                    }
                }

        disposable.add(disposableContactBlockOrDelete)

        binding.switchActivateCustomNotification.setOnCheckedChangeListener { _, isChecked ->
            activateCustomNotification(isChecked)

        }

        binding.optionActivateCustomNotification.setOnClickListener {
            binding.switchActivateCustomNotification.apply {
                isChecked = !isChecked
            }
        }

        binding.optionNotificationTone.setOnClickListener { openNotificationTone() }

        disableOptions()

        validateStateNotification()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    private fun validateStateNotification() {
        val state = args.contact.stateNotification
        activateCustomNotification(state)
        binding.switchActivateCustomNotification.isChecked = state
        if (state)
            updateSoundDefaultChannel()
    }

    private fun setDefaultNotificationOption(isChecked: Boolean) {
        Timber.d("*TestSetDefaultNotificationOption")
        if (!isChecked) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val soundTitle =
                RingtoneManager.getRingtone(context, uri)
                    .getTitle(requireContext())

            binding.textViewCurrentTone.text = soundTitle
        }
    }

    private fun activateCustomNotification(isChecked: Boolean) {

        if (isChecked) {

            enableOptions()

            createDefaultChannel()

            Handler(Looper.getMainLooper())
                .postDelayed(
                    {
                        updateSoundDefaultChannel()
                    }, 290
                )

        } else {

            setDefaultNotificationOption(isChecked)

            disableOptions()

            deleteChannel()

        }

    }

    private fun createDefaultChannel() {

        val channelId = handlerNotificationChannel.getChannelId(
            Constants.ChannelType.CUSTOM.type,
            args.contact.id,
            args.contact.getNickName()
        )

        val notificationChannel = handlerNotificationChannel.getChannel(channelId)

        if (notificationChannel == null) {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            handlerNotificationChannel.updateChannel(
                uri,
                Constants.ChannelType.CUSTOM.type,
                args.contact.id,
                args.contact.getNickName()
            )
        }
    }

    private fun deleteChannel() {
        val channel = handlerNotificationChannel.getChannelId(
            Constants.ChannelType.CUSTOM.type,
            args.contact.id,
            args.contact.getNickName()
        )

        Timber.d("")

        handlerNotificationChannel.deleteChannel(channel, args.contact.id)
    }

    private fun updateSoundDefaultChannel() {
        currentSoundNotificationMessage = handlerNotificationChannel.getChannelSound(
            Constants.ChannelType.CUSTOM.type,
            args.contact.id,
            args.contact.getNickName()
        )

        Timber.d("*TestChannelSound: current $currentSoundNotificationMessage")

        val soundTitle = RingtoneManager
            .getRingtone(
                requireContext(),
                currentSoundNotificationMessage
            )
            .getTitle(requireContext())

        Timber.d("*TestChannelSound: soundTitle $soundTitle")

        binding.textViewCurrentTone.text = soundTitle
    }

    private fun openNotificationTone() {
        Timber.d("*TestChannelSound: currentSoundNotificationMessage=$currentSoundNotificationMessage")
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
            Timber.d("*TestChannelSound: onActivityResult=$uri")
            handlerNotificationChannel.updateChannel(
                uri,
                Constants.ChannelType.CUSTOM.type,
                args.contact.id,
                args.contact.getNickName()
            )
            Handler(Looper.getMainLooper()).postDelayed({ updateSoundDefaultChannel() }, 290)
        }
    }

    private fun enableOptions() {
        binding.optionNotificationTone.isClickable = true
        binding.textViewNotificationToneTitle.isEnabled = true
        binding.textViewCurrentTone.isEnabled = true
    }

    private fun disableOptions() {
        binding.optionNotificationTone.isClickable = false
        binding.textViewNotificationToneTitle.isEnabled = false
        binding.textViewCurrentTone.isEnabled = false
    }
}