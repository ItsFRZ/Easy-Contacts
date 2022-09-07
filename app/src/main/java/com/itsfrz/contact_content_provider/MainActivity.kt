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

//        val context = this


//        CoroutineScope(Dispatchers.IO).launch {
//            val contact = Contact(
//                "",
//                "Demo Four",
//                "1234567890",
//                "",
//                "",
//                "OneTwo",
//                "Engineer",
//                "Macbook Pro",
//                "",
//                "onetwo@google.com",
//                "India",
//                "12345"
//            )
//            ContactProvider.insertContact(context, contact = contact)
//        }


//        CoroutineScope(Dispatchers.IO).launch {
//            val contacts: List<Contact> = ContactProvider.getContacts(context)
//            withContext(Dispatchers.Main){
//                contacts.forEach {
//                    Log.d("ContactList", "onCreate: ${it.toString()}")
//                }
//            }
//        }


        /*
            contactId=8, contactName=Demo 1
            contactId=9, contactName=Demo 2
            contactId=10, contactName=Demo 3
         */

//        CoroutineScope(Dispatchers.IO).launch {
//            val updateContact = Contact(
//                contactId = "28242",
//                contactName = "Demo Six",
//                contactNumber = "0987654321",
//                contactImage = "",
//                contactThumbnailImage = "",
//                contactOrganization = "Google",
//                contactJobTitle = "Software Engineer",
//                contactAddress = "New Journal Street, LA",
//                contactEmailId = "demo@shawn.com",
//                contactCountry = "IN",
//                contactPostalCode = "123456",
//                contactWebAddress = "https://www.google.com"
//            )
//            ContactProvider.updateContact(
//                context,
//                updateContact
//            )
//        }

//        CoroutineScope(Dispatchers.IO).launch {
//            val contact = ContactProvider.searchContactById(context, "27972")
//            Log.d("SEARCH", "onCreate: ${contact.toString()}")
//        }

//        Demo Five 28240

//        contactId=28240, contactName=Demo Five, contactNumber=55555522222,
//        contactId=28242, contactName=Demo Six, contactNumber=0987654321
//        CoroutineScope(Dispatchers.IO).launch {
//            val result = ContactProvider.deleteContact(
//                context, Contact(
//                    contactId = "28240",
//                    "Demo Five",
//                    "55555522222",
//                    "",
//                    "",
//                    "OneTwo",
//                    "Engineer",
//                    "Macbook Pro",
//                    "",
//                    "onetwo@google.com",
//                    "India",
//                    "12345"
//                )
//            )
//
//            Log.d("DELETE", "onCreate: ${if(result) "Deleted" else "Not Deleted"}")
//        }


    }
}