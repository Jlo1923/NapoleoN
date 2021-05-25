package com.naposystems.napoleonchat.service.multiattachment

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.service.multiattachment.contract.IContractMultipleUpload
import com.naposystems.napoleonchat.service.multiattachment.notification.NotificationMultiUploadClientImpl
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.source.local.entity.MessageEntity
import com.naposystems.napoleonchat.utility.Constants
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class MultipleUploadService : Service() {

    companion object {
        const val PROGRESS_MAX = 100
        const val MESSAGE_KEY = "message"
        const val ATTACHMENT_KEY = "attachment"
        const val ACTION_CANCEL_UPLOAD = "action_cancel_upload"
    }

    @Inject
    lateinit var repository: IContractMultipleUpload.Repository

    @Inject
    lateinit var notificationUploadService: NotificationMultiUploadClientImpl

    private lateinit var napoleonApplication: NapoleonApplication
    private val compositeDisposable = CompositeDisposable()
    var attachmentList: MutableList<Pair<MessageEntity?, AttachmentEntity>> = mutableListOf()
    var currentMessagePairData: Pair<MessageEntity?, AttachmentEntity>? = null

    override fun onCreate() {
        super.onCreate()
        (applicationContext as DaggerApplication).androidInjector().inject(this)
        this.napoleonApplication = applicationContext as NapoleonApplication
        subscribeRxEvents()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        getMessageAndAttachmentsFromExtras(intent)
        getCommandAction(intent)
        handleTryNextAttachment()
        return START_NOT_STICKY
    }

    private fun getCommandAction(intent: Intent) {
        intent.action?.let { action ->
            Timber.d("onStartCommand action: $action")
            if (action == ACTION_CANCEL_UPLOAD) {
                repository.cancelUpload()
                stopService()
            }
        }
    }

    private fun handleTryNextAttachment() {
        /**
         * Antes de tomar un nuevo attachment, debemos validar el padre del attachment que estabamos tratando
         * debemos validar si todos los attachments del mensaje padre fueron tratados como SENT
         * de ser asi, el mensaje padre debemos marcarlos como SENT
         */
        val nextAttachment = getNextAttachmentForTryUpload()
        nextAttachment?.let { pair ->
            currentMessagePairData = pair
            pair.first?.let { repository.uploadAttachment(pair.second, it) }
            showNotification(pair)
        } ?: run { handleUploadSuccess() }
    }

    private fun getMessageAndAttachmentsFromExtras(intent: Intent) {
        intent.extras?.let { bundle ->
            val message = bundle.getParcelable(MESSAGE_KEY) as MessageEntity?
            val attachmentsIn =
                bundle.getParcelableArrayList<AttachmentEntity>(ATTACHMENT_KEY) as List<AttachmentEntity>

            /**
             * Al momento de iniciar el proceso de subir archivos, debemos marcar el mensaje padre en estado SENDING
             * y cada uno de sus Attachments como ERROR, para que a medida que tome UNO, lo marque
             * como SENDING.
             *
             * Debemos agregar a la lista los elementos que no esten en ella para evitar duplicidad
             */
            message?.let {
                it.status = Constants.MessageStatus.SENDING.status
                repository.updateMessage(it)
            }
            val forList = attachmentsIn.map { Pair(message, it) }.toList()
            forList.forEach {
                if (attachmentList.contains(it).not()) {
                    it.second.status = Constants.AttachmentStatus.ERROR.status
                    repository.updateAttachment(it.second)
                    attachmentList.add(it)
                }
            }
        }
    }

    private fun getNextAttachmentForTryUpload(): Pair<MessageEntity?, AttachmentEntity>? {
        return attachmentList.firstOrNull() {
            it.second.isCancelUpload() || it.second.isError()
        }
    }

    private fun showNotification(pair: Pair<MessageEntity?, AttachmentEntity>) {
        pair.first?.let {
            val notification =
                notificationUploadService.createUploadNotification(applicationContext, it.id)
            Timber.d("notificationId: ${it.id}")
            startForeground(it.id, notification)
        }
    }

    private fun subscribeRxEvents() {

        val disposableUploadStart = RxBus.listen(RxEvent.MultiUploadStart::class.java)
            .subscribe { Timber.d("RxEvent.UploadStart") }

        val disposableUploadSuccess = RxBus.listen(RxEvent.MultiUploadSuccess::class.java)
            .subscribe { handleUploadSuccess() }

        val disposableUploadTryNext = RxBus.listen(RxEvent.MultiUploadTryNextAttachment::class.java)
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

    private fun handleUploadSuccess() {
        Timber.d("RxEvent.UploadSuccess")
        stopService()
    }

    private fun handleUploadError() {
        Timber.d("RxEvent.UploadError")
        stopService()
    }

    private fun handleUploadProgress(event: RxEvent.MultiUploadProgress) {
        currentMessagePairData?.first?.let {
            notificationUploadService.updateUploadNotificationProgress(
                PROGRESS_MAX, event.progress.toInt(), it.id
            )
        }
    }

    private fun stopService() {
        currentMessagePairData?.first?.let {
            notificationUploadService.cancelNotification(it.id)
        }
        stopSelf()
        stopForeground(true)
        compositeDisposable.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onBind(intent: Intent?): IBinder? = null

}