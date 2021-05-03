package com.naposystems.napoleonchat.service.download

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.download.contract.IContractDownloadService
import com.naposystems.napoleonchat.service.download.notification.NOTIFICATION_DOWNLOADING_MULTI
import com.naposystems.napoleonchat.service.download.notification.NotificationDownloadClient
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.AttachmentStatus.NOT_DOWNLOADED
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

const val ATTACHMENT_KEY = "attachment"

class DownloadAttachmentsService : Service(), IContractDownloadService.Service {

    companion object {
        const val PROGRESS_MAX = 100
        const val ACTION_CANCEL_DOWNLOAD = "action_cancel_download"
    }

    @Inject
    lateinit var repository: IContractDownloadService.Repository

    @Inject
    lateinit var notificationDownloadClient: NotificationDownloadClient

    private lateinit var napoleonApplication: NapoleonApplication
    private val notificationId = NOTIFICATION_DOWNLOADING_MULTI
    private val compositeDisposable = CompositeDisposable()
    lateinit var attachmentList: List<AttachmentEntity>

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DaggerApplication).androidInjector().inject(this)
        this.napoleonApplication = applicationContext as NapoleonApplication
        subscribeRxEvents()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")

        intent.extras?.let { bundle ->
            attachmentList =
                bundle.getParcelableArrayList<AttachmentEntity>(ATTACHMENT_KEY) as List<AttachmentEntity>
            handleTryNextAttachment()
        }

        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            if (action == ACTION_CANCEL_DOWNLOAD) {
                repository.cancelDownload()
                stopSelf()
                stopForeground(true)
            }
        }

        return START_NOT_STICKY
    }

    override fun showNotification() {
        notificationDownloadClient.createDownloadNotification(applicationContext).apply {
            startForeground(notificationId, this)
        }
    }

    override fun subscribeRxEvents() {

        val disposableUploadStart = RxBus.listen(RxEvent.MultiUploadStart::class.java)
            .subscribe { Timber.d("RxEvent.UploadStart") }

        val disposableUploadSuccess = RxBus.listen(RxEvent.MultiUploadSuccess::class.java)
            .subscribe { handleDownloadSuccess() }

        val disposableUploadTryNext =
            RxBus.listen(RxEvent.MultiDownloadTryNextAttachment::class.java)
                .subscribe { handleTryNextAttachment() }

        val disposableUploadError = RxBus.listen(RxEvent.MultiUploadError::class.java)
            .subscribe { handleUploadError() }

        val disposableUploadProgress = RxBus.listen(RxEvent.MultiUploadProgress::class.java)
            .subscribe { handleUploadProgress(it) }

        val disposableCompressProgress = RxBus.listen(RxEvent.MultiCompressProgress::class.java)
            .subscribe { }

        compositeDisposable.apply {
            addAll(
                disposableUploadStart,
                disposableUploadSuccess,
                disposableUploadTryNext,
                disposableUploadError,
                disposableUploadProgress,
                disposableCompressProgress
            )
        }
    }

    private fun handleDownloadSuccess() {
        Timber.d("RxEvent.UploadSuccess")
        stopService()
    }

    private fun handleTryNextAttachment() {
        val nextAttachment = getNextAttachment()
        nextAttachment?.let {
            repository.downloadAttachment(it)
            showNotification()
        } ?: run { handleDownloadSuccess() }
    }

    private fun handleUploadError() {
        stopService()
    }

    private fun handleUploadProgress(it: RxEvent.MultiUploadProgress) =
        notificationDownloadClient.updateDownloadNotificationProgress(
            MultipleUploadService.PROGRESS_MAX,
            it.progress.toInt()
        )

    private fun stopService() {
        stopSelf()
        stopForeground(true)
    }

    private fun getNextAttachment(): AttachmentEntity? =
        attachmentList.firstOrNull() {
            it.status == NOT_DOWNLOADED.status
        }

}