package com.naposystems.pepito.db.dao.quoteMessage

import androidx.room.Dao
import androidx.room.Insert
import com.naposystems.pepito.entity.message.Quote

@Dao
interface QuoteDao {

    @Insert
    fun insertQuote(quote: Quote)
}