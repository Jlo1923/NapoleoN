package com.naposystems.napoleonchat.service.download

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.download.contract.IContractDownloadService
import com.naposystems.napoleonchat.service.download.notification.NotificationDownloadClient
import com.naposystems.napoleonchat.service.multiattachment.MultipleUploadService
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
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
    private val compositeDisposable = CompositeDisposable()
    private var attachmentList: MutableList<AttachmentEntity> = mutableListOf()
    private var currentAttachment: AttachmentEntity? = null

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DaggerApplication).androidInjector().inject(this)
        this.napoleonApplication = applicationContext as NapoleonApplication
        subscribeRxEvents()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        getAttachmentFromExtras(intent)
        getCommandAction(intent)
        return START_NOT_STICKY
    }

    private fun getAttachmentFromExtras(intent: Intent) = intent.extras?.let { bundle ->
        val attachmentsIn =
            bundle.getParcelableArrayList<AttachmentEntity>(ATTACHMENT_KEY) as List<AttachmentEntity>
        attachmentsIn.forEach { attachToAdd ->
            val toAdd = attachmentList.firstOrNull() { attachInList ->
                attachInList.id == attachToAdd.id
            }
            if (toAdd == null) {
                attachmentList.add(attachToAdd)
            }
            if (attachmentList.size == 1) {
                handleTryNextAttachment()
            }
        }
    }

    private fun getCommandAction(intent: Intent) = intent.action?.let { action ->
        Timber.d("onStartCommand action: $action")
        if (action == ACTION_CANCEL_DOWNLOAD) {
            repository.cancelDownload()
            stopSelf()
            stopForeground(true)
        }
    }

    override fun showNotification(attachmentEntity: AttachmentEntity) {
        notificationDownloadClient.createDownloadNotification(
            applicationContext,
            attachmentEntity.messageId
        ).apply {
            startForeground(attachmentEntity.messageId, this)
        }
    }

    override fun subscribeRxEvents() {

        val disposableUploadStart = RxBus.listen(RxEvent.MultiUploadStart::class.java)
            .subscribe { Timber.d("RxEvent.UploadStart") }

//        val disposableUploadSuccess = RxBus.listen(RxEvent.MultiUploadSuccess::class.java)
//            .subscribe { handleDownloadSuccess() }

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
                //disposableUploadSuccess,
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
            currentAttachment = it
            repository.downloadAttachment(it)
            showNotification(it)
        } ?: run { handleDownloadSuccess() }
    }

    private fun handleUploadError() {
        stopService()
    }

    private fun handleUploadProgress(event: RxEvent.MultiUploadProgress) {
        currentAttachment?.let {
            notificationDownloadClient.updateDownloadNotificationProgress(
                MultipleUploadService.PROGRESS_MAX,
                event.progress.toInt(),
                it.messageId
            )
        }
    }

    private fun stopService() {
        currentAttachment?.let {
            notificationDownloadClient.cancelNotification(it.messageId)
        }
        stopSelf()
        stopForeground(true)
        compositeDisposable.dispose()
    }

    private fun getNextAttachment(): AttachmentEntity? =
        attachmentList.firstOrNull() { it.isNotDownloaded() || it.isCancelUpload() }

}