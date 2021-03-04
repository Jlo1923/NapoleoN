package com.naposystems.napoleonchat.di.module.sources.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.source.local.db.NapoleonRoomDatabase
import com.naposystems.napoleonchat.source.local.entity.StatusEntity
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
                            StatusEntity(1, context.getString(R.string.text_status_available)),
                            StatusEntity(2, context.getString(R.string.text_status_busy)),
                            StatusEntity(3, context.getString(R.string.text_status_in_meeting)),
                            StatusEntity(4, context.getString(R.string.text_status_only_messages)),
                            StatusEntity(5, context.getString(R.string.text_status_sleeping)),
                            StatusEntity(6, context.getString(R.string.text_status_only_emergency))
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