package com.naposystems.napoleonchat.db.dao.quoteMessage

import androidx.room.Dao
import androidx.room.Insert
import com.naposystems.napoleonchat.entity.message.Quote

@Dao
interface QuoteDao {

    @Insert
    fun insertQuote(quote: Quote)
}