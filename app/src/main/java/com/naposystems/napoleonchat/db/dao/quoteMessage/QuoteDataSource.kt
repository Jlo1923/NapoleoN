package com.naposystems.napoleonchat.db.dao.quoteMessage

import com.naposystems.napoleonchat.entity.message.Quote

interface QuoteDataSource {
    fun insertQuote(quote: Quote)
}