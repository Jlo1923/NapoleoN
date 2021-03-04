package com.naposystems.napoleonchat.source.local.datasource.quoteMessage

import com.naposystems.napoleonchat.source.local.entity.QuoteEntity
import com.naposystems.napoleonchat.source.local.dao.QuoteDao
import javax.inject.Inject

class QuoteLocalDataSourceImp @Inject constructor(
    private val quoteDao: QuoteDao
) : QuoteLocalDataSource {

    override fun insertQuote(quoteEntity: QuoteEntity) {
        quoteDao.insertQuote(quoteEntity)
    }
}