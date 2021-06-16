package com.naposystems.napoleonchat.source.local.datasource.attachment

import androidx.lifecycle.LiveData
import com.naposystems.napoleonchat.source.local.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

interface AttachmentLocalDataSource {

    fun insertAttachment(attachmentEntity: AttachmentEntity): Long

    fun insertAttachments(listAttachmentEntity: List<AttachmentEntity>): List<Long>

    fun updateAttachment(attachmentEntity: AttachmentEntity)

    suspend fun suspendUpdateAttachment(attachmentEntity: AttachmentEntity)

    fun updateAttachmentStatus(webId: String, state: Int)

    suspend fun updateAttachmentStatus(attachmentsWebIds: List<String>, status: Int)

    suspend fun deletedAttachments(attachmentsWebIds: List<String>)

    fun existAttachmentByWebId(id: String): Boolean

    fun existAttachmentById(id: String): Boolean

    suspend fun getAttachmentByWebId(webId: String): AttachmentEntity?

    fun getAttachmentByWebIdLiveData(webId: String): LiveData<AttachmentEntity?>

    fun getAttachmentsSelfDestructionExpired(): List<AttachmentEntity>

    /**
     * Metodo que se encarga de setear un attachment como error, usualmente se usa cuando estamos
     * enviando varios archivos por medio de un multi Attachment
     *
     * Toma una copia del tiempo de autodeetruccion
     * setea el tiempo de autodestruccion por error
     * y Actualiza el attachment
     *
     * @param attachmentEntity: the Attachment
     */
    suspend fun markAttachmentAsError(attachmentEntity: AttachmentEntity)

}