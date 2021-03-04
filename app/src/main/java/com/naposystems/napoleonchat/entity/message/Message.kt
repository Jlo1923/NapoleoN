package com.naposystems.napoleonchat.entity.message

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.naposystems.napoleonchat.BuildConfig
import com.naposystems.napoleonchat.crypto.message.CryptoMessage
import com.naposystems.napoleonchat.entity.Contact
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "message", foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contact_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "web_id") val webId: String,
    @ColumnInfo(name = "uuid") val uuid: String?,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "quoted") val quoted: String,
    @ColumnInfo(name = "contact_id") val contactId: Int,
    @ColumnInfo(name = "updated_at") var updatedAt: Int,
    @ColumnInfo(name = "created_at") val createdAt: Int,
    @ColumnInfo(name = "is_mine") val isMine: Int,
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "is_selected") var isSelected: Boolean = false,
    @ColumnInfo(name = "number_attachments") val numberAttachments: Int,
    @ColumnInfo(name = "self_destruction_at") var selfDestructionAt: Int = -1,
    @ColumnInfo(name = "total_self_destruction_at") var totalSelfDestructionAt: Int = 0,
    @ColumnInfo(name = "type_message") val messageType: Int,
    @ColumnInfo(name = "cypher") val cypher: Boolean = false
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

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
}