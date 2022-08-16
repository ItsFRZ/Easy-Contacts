package com.itsfrz.contact_content_provider

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.itsfrz.support.Contact
import com.itsfrz.support.provider.ContactProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val context = this


        CoroutineScope(Dispatchers.IO).launch {
            val contact = Contact(
                "",
                "OneTwo",
                "1111122222",
                "",
                "",
                "OneTwo",
                "Engineer",
                "Macbook Pro",
                "",
                "onetwo@google.com",
                "India",
                "12345"
            )
            ContactProvider.insertContact(context, contact = contact)
        }


        CoroutineScope(Dispatchers.IO).launch {
            val contacts: List<Contact> = ContactProvider.getContacts(context)
            withContext(Dispatchers.Main){
                contacts.forEach {
                    Log.d("ContactList", "onCreate: ${it.toString()}")
                }
            }
        }


    }
}