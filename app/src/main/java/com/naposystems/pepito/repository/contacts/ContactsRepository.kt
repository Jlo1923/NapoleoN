package com.naposystems.pepito.repository.contacts

import android.content.Context
import com.naposystems.pepito.R
import com.naposystems.pepito.entity.Contact
import com.naposystems.pepito.ui.contacts.IContractContacts
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import javax.inject.Inject

class ContactsRepository @Inject constructor(private val context: Context) :
    IContractContacts.Repository {

    override suspend fun getContacts(): List<Contact> {
        val contacts = context.resources
            .openRawResource(R.raw.contacts)
            .bufferedReader()
            .use { it.readLine() }

        val moshi = Moshi.Builder().build()

        val listType = Types.newParameterizedType(List::class.java, Contact::class.java)
        val adapter: JsonAdapter<List<Contact>> = moshi.adapter(listType)

        return adapter.fromJson(contacts)!!
    }
}