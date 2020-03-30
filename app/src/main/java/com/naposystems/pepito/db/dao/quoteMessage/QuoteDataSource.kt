package com.naposystems.pepito.db.dao.quoteMessage

import com.naposystems.pepito.entity.message.Quote

interface QuoteDataSource {
    fun insertQuote(quote: Quote)
}