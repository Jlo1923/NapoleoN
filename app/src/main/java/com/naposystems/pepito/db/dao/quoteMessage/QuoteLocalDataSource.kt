package com.naposystems.pepito.db.dao.quoteMessage

import com.naposystems.pepito.entity.message.Quote
import javax.inject.Inject

class QuoteLocalDataSource @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteDataSource {

    override fun insertQuote(quote: Quote) {
        quoteDao.insertQuote(quote)
    }
}