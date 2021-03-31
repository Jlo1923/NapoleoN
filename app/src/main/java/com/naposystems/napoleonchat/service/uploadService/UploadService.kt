package com.naposystems.napoleonchat.service.uploadService

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.notificationUpload.NotificationUploadService
import com.naposystems.napoleonchat.service.notificationUpload.NotificationUploadServiceImp
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class UploadService() : Service(), IContractUploadService {

    companion object {
        const val PROGRESS_MAX = 100
        const val MESSAGE_KEY = "message"
        const val ATTACHMENT_KEY = "attachment"
        const val ACTION_CANCEL_UPLOAD = "action_cancel_upload"
    }

    @Inject
    lateinit var repository: IContractUploadService.Repository

    @Inject
    lateinit var notificationUploadService: NotificationUploadService

    private lateinit var napoleonApplication: NapoleonApplication

    private val notificationId = NotificationUploadServiceImp.NOTIFICATION_UPLOADING

    private val compositeDisposable = CompositeDisposable()

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

        var messageEntity: MessageEntity? = null
        var attachmentEntity: AttachmentEntity? = null

        intent.extras?.let { bundle ->
            messageEntity = bundle.getParcelable(MESSAGE_KEY) as MessageEntity?
            attachmentEntity = bundle.getParcelable(ATTACHMENT_KEY) as AttachmentEntity?
        }

        if (messageEntity != null && attachmentEntity != null) {
            repository.uploadAttachment(attachmentEntity!!, messageEntity!!)
            showNotification()
        }

        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            if (action == ACTION_CANCEL_UPLOAD) {
                repository.cancelUpload()
                stopSelf()
                stopForeground(true)
            }
        }

        return START_NOT_STICKY
    }

    private fun showNotification() {
        val notification = notificationUploadService.createUploadNotification(
            applicationContext
        )

        Timber.d("notificationId: $notificationId")

        startForeground(notificationId, notification)
    }

    private fun subscribeRxEvents() {
        val disposableUploadStart = RxBus.listen(RxEvent.UploadStart::class.java)
            .subscribe {
                Timber.d("RxEvent.UploadStart")
            }

        val disposableUploadSuccess = RxBus.listen(RxEvent.UploadSuccess::class.java)
            .subscribe {
                Timber.d("RxEvent.UploadSuccess")
                stopSelf()
                stopForeground(true)
            }

        val disposableUploadError = RxBus.listen(RxEvent.UploadError::class.java)
            .subscribe {
                Timber.d("RxEvent.UploadError")
                stopSelf()
                stopForeground(true)
            }

        val disposableUploadProgress = RxBus.listen(RxEvent.UploadProgress::class.java)
            .subscribe {
                notificationUploadService.updateUploadNotificationProgress(PROGRESS_MAX, it.progress.toInt())
            }

        val disposableCompressProgress = RxBus.listen(RxEvent.CompressProgress::class.java)
            .subscribe {
            }

        compositeDisposable.add(disposableUploadStart)
        compositeDisposable.add(disposableUploadSuccess)
        compositeDisposable.add(disposableUploadError)
        compositeDisposable.add(disposableUploadProgress)
        compositeDisposable.add(disposableCompressProgress)
    }
}