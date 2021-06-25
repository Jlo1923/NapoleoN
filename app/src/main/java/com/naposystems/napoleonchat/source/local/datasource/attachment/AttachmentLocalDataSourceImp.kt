package com.naposystems.napoleonchat.source.local.datasource.attachment

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.dao.AttachmentDao
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.local.dao.MessageDao
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Constants.SharedPreferences.PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT
import com.naposystems.napoleonchat.utility.SharedPreferencesManager
import com.naposystems.napoleonchat.utility.Utils
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AttachmentLocalDataSourceImp @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val contactDao: ContactDao,
    private val preferencesManager: SharedPreferencesManager,
    private val messageDao: MessageDao,
    private val context: Context
) : AttachmentLocalDataSource {

    override fun existAttachmentByWebId(webId: String): Boolean =
        attachmentDao.existAttachmentByWebId(webId) != null

    override fun existAttachmentById(id: String): Boolean =
        attachmentDao.existAttachmentById(id) != null

    override suspend fun getAttachmentByWebId(
        webId: String
    ): AttachmentEntity? = attachmentDao.getAttachmentByWebId(webId)

    override fun getAttachmentByWebIdLiveData(
        webId: String
    ): LiveData<AttachmentEntity?> = attachmentDao.getAttachmentByWebIdLiveData(webId)

    override fun getAttachmentsSelfDestructionExpired(): List<AttachmentEntity> =
        attachmentDao.getAttachmentsSelfDestructionExpired()

    override fun insertAttachment(attachmentEntity: AttachmentEntity): Long =
        attachmentDao.insertAttachment(attachmentEntity)

    override fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long> =
        attachmentDao.insertAttachments(listAttachmentEntity)

    override fun updateAttachment(attachmentEntity: AttachmentEntity) =
        attachmentDao.updateAttachment(attachmentEntity)

    override suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity) =
        attachmentDao.suspendUpdateAttachment(attachmentEntity)

    override fun updateAttachmentStatus(webId: String, state: Int) =
        attachmentDao.updateAttachmentState(webId, state)

    override suspend fun updateAttachmentStatus(attachmentsWebIds: List<String>, status: Int) {

        attachmentsWebIds.forEach { attachmentWebId ->

            val attachment = getAttachmentByWebId(attachmentWebId)

            attachment?.let { it ->

                if (it.status != Constants.MessageStatus.READED.status) {

                    val timeByAttachment =
                        attachmentDao.getAttachmentSelfDestructTimeById(attachmentWebId)

                    val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())

                    if (status == Constants.AttachmentStatus.READED.status) {

                        val time =
                            currentTime.plus(Utils.convertItemOfTimeInSeconds(timeByAttachment))

                        attachmentDao.updateAttachmentStatus(
                            attachmentWebId,
                            currentTime,
                            time,
                            status
                        )

                    } else {

                        if (timeByAttachment == Constants.SelfDestructTime.EVERY_TWENTY_FOUR_HOURS_ERROR.time ||
                            timeByAttachment == Constants.SelfDestructTime.EVERY_SEVEN_DAYS_ERROR.time
                        ) {

                            val contactId = messageDao.getContactIdByWebId(it.messageWebId)

                            val timeContact =
                                contactDao.getSelfDestructTimeByContactWithOutLiveData(
                                    contactId
                                )

                            val durationAttachment = TimeUnit.MILLISECONDS.toSeconds(
                                attachment.duration ?: 0
                            ).toInt()

                            val selfAutoDestruction =
                                Utils.compareDurationAttachmentWithSelfAutoDestructionInSeconds(
                                    durationAttachment, timeContact
                                )

                            attachmentDao.updateSelfDestructTimeByAttachments(
                                selfAutoDestruction,
                                attachmentWebId,
                                status
                            )

                        } else {

                            attachmentDao.updateAttachmentStatus(
                                attachmentWebId,
                                0,
                                0,
                                status
                            )

                        }

                    }
                }
            }
        }

    }

    override suspend fun deletedAttachments(attachmentsWebIds: List<String>) {

        var messageWebId = ""

        attachmentsWebIds.forEach { webId ->
            attachmentDao.getAttachmentByWebId(webId)?.let { attachmentEntity ->
                messageWebId = attachmentEntity.messageWebId
                attachmentEntity.deleteFile(context)
                attachmentDao.deletedAttachment(webId)
            }
        }

        /**
         * Al eliminar los attachments, debemos validar si el mensaje se queda sin attachments
         * de ser asi, eliminamos el mensaje
         * De no ser asi, actualizamos su numberAttachments
         */
        val messageParent = messageDao.getMessageByWebId(messageWebId)
        messageParent?.let {
            if (it.attachmentEntityList.isEmpty() && it.messageEntity.numberAttachments > 0) {
                messageDao.deleteMessagesByWebId(it.messageEntity.webId)
            } else {
                var newNumberAttachments = it.messageEntity.numberAttachments - 1

                //esto para evitar que se incremente en -1 el numberAttachments en el mensaje de bienvenida
                //Y para los mensajes no enviados(falla)
                if (it.messageEntity.messageType == Constants.MessageTextType.NEW_CONTACT.type || it.messageEntity.status == Constants.MessageStatus.ERROR.status)
                    newNumberAttachments = 0

                val msgCopy = it.messageEntity.copy(numberAttachments = newNumberAttachments)
                messageDao.updateMessage(msgCopy)
            }
        }

    }

    override suspend fun markAttachmentAsError(
        attachmentEntity: AttachmentEntity
    ) {

        preferencesManager.putInt("$attachmentEntity.id", attachmentEntity.id)

        val selfDestructTime = preferencesManager.getInt(PREF_MESSAGE_SELF_DESTRUCT_TIME_NOT_SENT)
        val currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()

        val attachmentInDb = attachmentDao.getAttachmentById(attachmentEntity.id.toString())

        attachmentInDb?.let {
            if (it.isSent().not()) {
                it.updatedAt = currentTime
                //it.selfDestructionAt = selfDestructTime
                it.totalSelfDestructionAt =
                    currentTime.plus(Utils.convertItemOfTimeInSecondsByError(selfDestructTime))
                it.status = Constants.AttachmentStatus.ERROR.status

                updateAttachment(it)
            }
        }
    }

}