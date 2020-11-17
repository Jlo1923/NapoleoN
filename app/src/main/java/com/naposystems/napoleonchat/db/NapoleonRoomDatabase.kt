package com.naposystems.napoleonchat.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.napoleonchat.db.dao.contact.ContactDao
import com.naposystems.napoleonchat.db.dao.message.MessageDao
import com.naposystems.napoleonchat.db.dao.attachment.AttachmentDao
import com.naposystems.napoleonchat.db.dao.quoteMessage.QuoteDao
import com.naposystems.napoleonchat.db.dao.status.StatusDao
import com.naposystems.napoleonchat.db.dao.user.UserDao
import com.naposystems.napoleonchat.entity.*
import com.naposystems.napoleonchat.entity.message.Message
import com.naposystems.napoleonchat.entity.message.Quote
import com.naposystems.napoleonchat.entity.message.attachments.Attachment

@Database(
    entities = [
        User::class, Status::class, Message::class,
        Attachment::class, Contact::class,
        Quote::class
    ],
    version = 1
)
abstract class NapoleonRoomDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun statusDao(): StatusDao

    abstract fun messageDao(): MessageDao

    abstract fun quoteDao(): QuoteDao

    abstract fun attachmentDao(): AttachmentDao

    abstract fun contactDao(): ContactDao

}