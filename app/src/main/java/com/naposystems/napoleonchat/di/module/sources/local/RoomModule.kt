package com.naposystems.napoleonchat.di.module.sources.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.entity.Status
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class RoomModule {

    lateinit var napoleonDB: NapoleonRoomDatabase

    @Provides
    @Singleton
    fun provideRoomDatabase(context: Context): NapoleonRoomDatabase {
        napoleonDB =
            Room.databaseBuilder(context, NapoleonRoomDatabase::class.java, "napoleon_database")
                .addMigrations(NapoleonRoomDatabase.MIGRATION_1_2)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        val prepopulateData = listOf(
                            Status(1, context.getString(R.string.text_status_available)),
                            Status(2, context.getString(R.string.text_status_busy)),
                            Status(3, context.getString(R.string.text_status_in_meeting)),
                            Status(4, context.getString(R.string.text_status_only_messages)),
                            Status(5, context.getString(R.string.text_status_sleeping)),
                            Status(6, context.getString(R.string.text_status_only_emergency))
                        )

                        Executors.newSingleThreadExecutor().execute {
                            napoleonDB.statusDao().insertStatus(prepopulateData)
                        }
                    }
                })
                .allowMainThreadQueries()
                .build()

        return napoleonDB
    }

}