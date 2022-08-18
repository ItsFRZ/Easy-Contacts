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


//        CoroutineScope(Dispatchers.IO).launch {
//            val contact = Contact(
//                "",
//                "DemoFour",
//                "1111122222",
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


        CoroutineScope(Dispatchers.IO).launch {
            val contacts: List<Contact> = ContactProvider.getContacts(context)
            withContext(Dispatchers.Main){
                contacts.forEach {
                    Log.d("ContactList", "onCreate: ${it.toString()}")
                }
            }
        }



        /*


                contactId=28008, contactName=DemoFour
                contactId=27160, contactName=Alex Demo
                contactId=28004, contactName=Shawn
                contactId=28003, contactName=Ron
         */

        CoroutineScope(Dispatchers.IO).launch {
            val updateContact = Contact(
                contactId = "28007",
                contactName = "Shawn Michael",
                contactNumber = "9876543210",
                contactImage = "content://com.android.contacts/display_photo/18",
                contactThumbnailImage = "content://com.android.contacts/contacts/27161/photo",
                contactOrganization = "Google",
                contactJobTitle = "Software Engineer",
                contactAddress = "New Journal Street, LA",
                contactEmailId = "michael@shawn.com",
                contactCountry = "USA",
                contactPostalCode = "12345",
                contactWebAddress = "https://www.google.com"
            )
            ContactProvider.updateContact(
                context,
                updateContact
            )

        }
        CoroutineScope(Dispatchers.IO).launch {
            val contact = ContactProvider.searchContactById(context, "27972")
            Log.d("SEARCH", "onCreate: ${contact.toString()}")
        }


    }
}