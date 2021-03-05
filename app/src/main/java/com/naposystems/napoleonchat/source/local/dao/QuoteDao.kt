package com.naposystems.napoleonchat.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.naposystems.napoleonchat.source.local.entity.QuoteEntity

@Dao
interface QuoteDao {

    @Insert
    fun insertQuote(quoteEntity: QuoteEntity)
}