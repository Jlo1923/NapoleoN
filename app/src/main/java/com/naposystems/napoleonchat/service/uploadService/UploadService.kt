package com.naposystems.napoleonchat.service.uploadService

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.naposystems.napoleonchat.app.NapoleonApplication
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.attachments.Attachment
import com.naposystems.napoleonchat.reactive.RxBus
import com.naposystems.napoleonchat.reactive.RxEvent
import com.naposystems.napoleonchat.utility.UploadResult
import com.naposystems.napoleonchat.utility.notificationUtils.NotificationUtils
import dagger.android.support.DaggerApplication
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UploadService : Service(), IContractUploadService {

    companion object {
        const val PROGRESS_MAX = 100
        const val MESSAGE_KEY = "message"
        const val ATTACHMENT_KEY = "attachment"
    }

    @Inject
    lateinit var repository: IContractUploadService.Repository

    private lateinit var napoleonApplication: NapoleonApplication

    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.IO)

    private val _uploadProgress = MutableLiveData<UploadResult>()
    val uploadProgress: LiveData<UploadResult>
        get() = _uploadProgress

    private val notificationId = NotificationUtils.NOTIFICATION_UPLOADING

    val notificationUtils by lazy {
        NotificationUtils(
            applicationContext
        )
    }

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

        var message: Message? = null
        var attachment: Attachment? = null

        intent.extras?.let { bundle ->
            message = bundle.getParcelable(MESSAGE_KEY) as Message?
            attachment = bundle.getParcelable(ATTACHMENT_KEY) as Attachment?
        }

        if (message != null && attachment != null) {
            repository.uploadAttachment(attachment!!, message!!)
        }

        showNotification()
        return START_NOT_STICKY
    }

    private fun showNotification() {
        val notification = notificationUtils.createUploadNotification(
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
            }

        val disposableUploadProgress = RxBus.listen(RxEvent.UploadProgress::class.java)
            .subscribe {
                Timber.d("RxEvent.UploadProgress: ${it.progress}")
                notificationUtils.updateUploadProgress(PROGRESS_MAX, it.progress.toInt())
            }

        val disposableCompressProgress = RxBus.listen(RxEvent.CompressProgress::class.java)
            .subscribe {
                Timber.d("RxEvent.CompressProgress")
            }

        compositeDisposable.add(disposableUploadStart)
        compositeDisposable.add(disposableUploadSuccess)
        compositeDisposable.add(disposableUploadError)
        compositeDisposable.add(disposableUploadProgress)
        compositeDisposable.add(disposableCompressProgress)
    }
}