package com.itsfrz.support.provider

import android.content.Context
import com.itsfrz.support.Contact

interface ProviderService {
    suspend fun getContacts(context: Context) : List<Contact>

    suspend fun insertContact(context: Context, contact: Contact)

    suspend fun updateContact(context: Context, oldContact: Contact)

    suspend fun deleteContact(context: Context, contact: Contact) : Boolean

    suspend fun deleteContactList(context: Context, contacts: List<Contact>) : Boolean
}