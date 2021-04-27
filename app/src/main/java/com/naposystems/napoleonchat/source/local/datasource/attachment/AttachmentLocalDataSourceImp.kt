package com.naposystems.napoleonchat.source.local.datasource.attachment

import android.content.Context
import com.naposystems.napoleonchat.source.local.dao.AttachmentDao
import com.naposystems.napoleonchat.source.local.dao.ContactDao
import com.naposystems.napoleonchat.source.local.dao.MessageDao
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import com.naposystems.napoleonchat.utility.Constants
import com.naposystems.napoleonchat.utility.Utils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AttachmentLocalDataSourceImp @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val contactDao: ContactDao,
    private val messageDao: MessageDao,
    private val context: Context
) : AttachmentLocalDataSource {


    override fun existAttachmentByWebId(webId: String): Boolean {
        return attachmentDao.existAttachmentByWebId(webId) != null
    }

    override suspend fun getAttachmentByWebId(
        webId: String
    ): AttachmentEntity? {
        return attachmentDao.getAttachmentByWebId(webId)
    }

    override fun insertAttachment(attachmentEntity: AttachmentEntity): Long {
        return attachmentDao.insertAttachment(attachmentEntity)
    }

    override fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long> {
        return attachmentDao.insertAttachments(listAttachmentEntity)
    }

    override fun updateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentDao.updateAttachment(attachmentEntity)
    }

    override suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity) {
        attachmentDao.suspendUpdateAttachment(attachmentEntity)
    }

    override fun updateAttachmentStatus(webId: String, state: Int) {
        attachmentDao.updateAttachmentState(webId, state)
    }

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
        attachmentsWebIds.forEach { webId ->
            attachmentDao.getAttachmentByWebId(webId)?.let { attachmentEntity ->
                attachmentEntity.deleteFile(context)
                attachmentDao.deletedAttachment(webId)
            }
        }
        //TODO: JuankDev12 Aqui debes borrar el mensajes cuando todos los adjuntos de multiadjuntos debes eliminar el mensaje tambn
    }
}