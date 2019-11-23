package com.naposystems.pepito.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.naposystems.pepito.db.dao.user.UserDao
import com.naposystems.pepito.entity.User

@Database(entities = arrayOf(User::class), version = 1)
public abstract class NapoleonRoomDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

}