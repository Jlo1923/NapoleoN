package com.naposystems.napoleonchat.source.local.entity

import android.os.Parcelable
import androidx.room.*
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.source.local.DBConstants
import com.naposystems.napoleonchat.utility.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = DBConstants.Message.TABLE_NAME_MESSAGE,
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = [DBConstants.Contact.COLUMN_ID],
            childColumns = [DBConstants.Message.COLUMN_CONTACT_ID],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(
        value = [DBConstants.Message.COLUMN_UUID],
        unique = true
    )]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.Message.COLUMN_ID) var id: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_WEB_ID) val webId: String,
    @ColumnInfo(name = DBConstants.Message.COLUMN_UUID) val uuid: String?,
    @ColumnInfo(name = DBConstants.Message.COLUMN_BODY) var body: String,
    @ColumnInfo(name = DBConstants.Message.COLUMN_QUOTED) val quoted: String,
    @ColumnInfo(name = DBConstants.Message.COLUMN_CONTACT_ID) val contactId: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_UPDATED_AT) var updatedAt: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_CREATED_AT) val createdAt: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_IS_MINE) val isMine: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_STATUS) var status: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_IS_SELECTED) var isSelected: Boolean = false,
    @ColumnInfo(name = DBConstants.Message.COLUMN_NUMBER_ATTACHMENTS) val numberAttachments: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_SELF_DESTRUCTION_AT) var selfDestructionAt: Int = -1,
    @ColumnInfo(name = DBConstants.Message.COLUMN_TOTAL_SELF_DESTRUCTION_AT) var totalSelfDestructionAt: Int = 0,
    @ColumnInfo(name = DBConstants.Message.COLUMN_TYPE_MESSAGE) val messageType: Int,
    @ColumnInfo(name = DBConstants.Message.COLUMN_CYPHER) val cypher: Boolean = false
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageEntity

        if (id != other.id) return false
        if (webId != other.webId) return false
        if (status != other.status) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + webId.hashCode()
        return result
    }

    fun encryptBody(cryptoMessage: CryptoMessage) {
        this.body = cryptoMessage.encryptMessageBody(this.body)
    }

    fun getBody(cryptoMessage: CryptoMessage) =
        if (BuildConfig.ENCRYPT_API && this.cypher) {
            cryptoMessage.decryptMessageBody(this.body)
        } else {
            this.body
        }

    fun mustSendToRemote(): Boolean {
        return (status == Constants.MessageStatus.ERROR.status ||
                status == Constants.MessageStatus.SENDING.status)
                && webId.isEmpty()
    }

}