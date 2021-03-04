package com.naposystems.napoleonchat.source.local.datasource.quoteMessage

import com.naposystems.napoleonchat.source.local.entity.QuoteEntity

interface QuoteLocalDataSource {
    fun insertQuote(quoteEntity: QuoteEntity)
}