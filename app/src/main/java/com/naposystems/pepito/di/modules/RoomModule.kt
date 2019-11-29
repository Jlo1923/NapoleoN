package com.naposystems.pepito.di.modules

import android.content.Context
import androidx.room.Room
import com.naposystems.pepito.db.NapoleonRoomDatabase
import com.naposystems.pepito.db.dao.user.UserDao
import com.naposystems.pepito.db.dao.user.UserLocalDataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {

    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context): NapoleonRoomDatabase {
        return Room.databaseBuilder(context, NapoleonRoomDatabase::class.java, "napoleon_database")
            .addMigrations(
                NapoleonRoomDatabase.MIGRATION_1_2,
                NapoleonRoomDatabase.MIGRATION_2_3,
                NapoleonRoomDatabase.MIGRATION_3_4
            )
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(napoleonRoomDatabase: NapoleonRoomDatabase): UserDao {
        return napoleonRoomDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideUserLocalDataSource(userDao: UserDao): UserLocalDataSource {
        return UserLocalDataSource((userDao))
    }
}