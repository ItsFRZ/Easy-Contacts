package com.itsfrz.support.provider

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.RemoteException
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import android.util.Log
import com.itsfrz.support.Contact


object ContactProvider : ProviderService {

    private const val PROVIDER_ERROR = "PROVIDER_ERROR"
    private const val PROVIDER_DEBUG = "PROVIDER_DEBUG"


    private val mColumnProjectionsForPostal = arrayOf<String>(
        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
        ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
        ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY
    )

    private val mColumnProjectionForOrganization = arrayOf<String>(
        ContactsContract.CommonDataKinds.Organization.COMPANY,
        ContactsContract.CommonDataKinds.Organization.TITLE
    )
    private val mColumnProjectionForWebsite = arrayOf<String>(
        ContactsContract.CommonDataKinds.Website.URL
    )

    private val mColumnProjections = arrayOf<String>(
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
        ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI,
        ContactsContract.CommonDataKinds.Organization.COMPANY,
        ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
        ContactsContract.CommonDataKinds.Email.ADDRESS,
        ContactsContract.CommonDataKinds.Website.URL
    )

    /*
                    Get Contacts
    */

    override suspend fun getContacts(
        context: Context
    ): List<Contact> {

        val contentResolver: ContentResolver = context.contentResolver
        val contactList = ArrayList<Contact>()


        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            mColumnProjections,
            null,
            null,
            null
        )
        try {
            cursor?.let {
                if (it.count > 0) {
                    while (it.moveToNext()) {
                        val contact: Contact =
                            Contact("", "", "", "", "", "", "", "", "", "", "", "")
                        val contactId: String = it.getString(0)
                        val contactName = it.getString(1)
                        val contactNumber: String = it.getString(2)
                        val contactPhotoUri = getContactImage(it, 3)
                        val contactThumbnailPhotoUri = it.getString(4)

                        val contactOrganization: String = getDataFromCursor(it, 5, contactNumber)
                        val contactJobTitle: String = getDataFromCursor(it, 6, contactNumber)
                        val contactWebsite: String = getDataFromCursor(it, 8, contactNumber)

                        if (contactThumbnailPhotoUri == null) {
                            contact.apply {
                                this.contactId = contactId
                                this.contactThumbnailImage = ""
                                this.contactNumber = contactNumber
                                this.contactName = contactName
                                this.contactImage = contactPhotoUri
                                this.contactOrganization = contactOrganization
                                this.contactJobTitle = contactJobTitle
                                this.contactWebAddress = contactWebsite
                            }

                        } else {
                            contact.apply {
                                this.contactId = contactId
                                this.contactThumbnailImage = contactThumbnailPhotoUri
                                this.contactNumber = contactNumber
                                this.contactName = contactName
                                this.contactImage = contactPhotoUri
                                this.contactOrganization = contactOrganization
                                this.contactJobTitle = contactJobTitle
                                this.contactWebAddress = contactWebsite
                            }

                        }

                        val emailContact: Contact =
                            getEmailId(context, contact, contactId)

                        emailContact.let {
                            contact.contactEmailId = it.contactEmailId
                        }
                        val addressContact: Contact =
                            getAddress(context, contact, contactId)
                        addressContact.let {
                            contact.contactAddress = it.contactAddress
                            contact.contactCountry = it.contactCountry
                            contact.contactPostalCode = it.contactPostalCode
                        }
                        contactList.add(contact)
                    }
                    it.close()
                }
            }
        } catch (e: Exception) {
            Log.e("EXCEPTION", "getContactList: $e")
        } finally {
            cursor?.close()
        }
        return contactList

    }

    private fun getDataFromCursor(cursor: Cursor, index: Int, contactNumber: String): String {
        val data = cursor.getString(index)?.toString() ?: ""
        if (data.contains(contactNumber))
            return ""
        return data
    }


    private fun getContactImage(cursor: Cursor, index: Int): String {
        val data = cursor.getString(index)
        data?.let {
            if (it.toString().isNotBlank())
                return it.toString()
        }
        return ""
    }

    private fun getAddress(
        context: Context,
        contact: Contact,
        contactId: String,
    ): Contact {
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            mColumnProjectionsForPostal,
            whereClause,
            arrayOf<String>(contactId),
            null
        )

        var address: String? = ""
        var pincode: String? = ""
        var country: String? = ""
        if (cursor != null && cursor.count > 0) {
            try {
                while (cursor.moveToNext()) {
                    address = cursor.getString(0)
                    pincode = cursor.getString(1) ?: ""
                    country = cursor.getString(2)
                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "getAddress: ${e}")
            } finally {
                cursor.close()
            }
        }

        if (address != null) {
            contact.contactAddress = address.toString()
        }

        if (country != null) {
            contact.contactCountry = country.toString()
        }

        if (pincode != null) {
            contact.contactPostalCode = pincode.toString()
        }
//        Log.d("ADDRESS", "getAddress: $contact")
        return contact
    }

    private fun getEmailId(
        context: Context,
        contact: Contact,
        contactId: String,
    ): Contact {
        val whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"

        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf<String>(ContactsContract.CommonDataKinds.Email.ADDRESS),
            whereClause,
            arrayOf<String>(contactId),
            null
        )

        var emailId: String? = ""
        if (cursor != null && cursor.count > 0) {
            try {
                while (cursor.moveToNext()) {
                    emailId = cursor.getString(0)
                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "getEmailId: ${e}")
            } finally {
                cursor.close()
            }
        }

        if (emailId != null) {
            contact.contactEmailId = emailId.toString()
        }

        return contact
    }

    /*
                    Insert Contact
    */

    override suspend fun insertContact(context: Context, contact: Contact) {


//        val where: String = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
//        val params: Array<String> = arrayOf<String>(
//            contact.contactNumber
//        )
//
//        val contentResolver = context.contentResolver
//        val cursor = contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            null,
//            where,
//            params,
//            null
//        )
//        if (cursor != null) {
//            val gotContact = searchContactByNumber(context, contact.contactNumber)
//            contact.contactId = gotContact.contactId
//            Log.d(
//                PROVIDER_DEBUG,
//                "insertContact: Contact Already Present With Contact Number ${contact.contactNumber} & Contact Id ${contact.contactId}"
//            )
//            updateContact(context, contact)
//            return
//        }

        val ops = arrayListOf<ContentProviderOperation>()
        var rawContactInsertIndex = ops.size;

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
        );

        //Phone Number
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.contactNumber)
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, "1").build()
        );

        //Display name/Contact name
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    contact.contactName
                )
                .build()
        );

        //Email details
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Email.DATA, contact.contactEmailId)
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, "2").build()
        );


        //Postal Address

        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POBOX, "")

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, "")

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                    contact.contactAddress
                )

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, "")

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                    contact.contactPostalCode
                )

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                    contact.contactCountry
                )

                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, "3")

                .build()
        );


        //Organization details
        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                    ContactsContract.Contacts.Data.RAW_CONTACT_ID,
                    rawContactInsertIndex
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Organization.COMPANY,
                    contact.contactOrganization
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                )
                .withValue(
                    ContactsContract.CommonDataKinds.Organization.TITLE,
                    contact.contactJobTitle
                )
                .withValue(
                    ContactsContract.Contacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, "0")
                .build()
        )

        ops.add(
            ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,contact.contactImage).build()
        )
        try {
            val res: Array<ContentProviderResult> = context.contentResolver.applyBatch(
                ContactsContract.AUTHORITY, ops
            )
            Log.d(
                "INSERTION",
                "insertContact: ${contact.contactName} Contact Inserted Successfully"
            )
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            e.printStackTrace()
        }


    }

    /*
                    Update Contact
    */


    // Only name and number is updating currently // work on it
    override suspend fun updateContact(context: Context, contact: Contact) {

        val ops = ArrayList<ContentProviderOperation>()
        val contactId: String = contact.contactId

        val contactName = contact.contactName.toString()
        // Name
        var firstName = if (contactName.contains(" ")) contactName.split(" ")[0] else contactName
        var lastName = if (contactName.contains(" ")) contactName.split(" ")[1] else ""

        // Name
        var builder: ContentProviderOperation.Builder


        // Name
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
        ops.add(builder.build())

        // Number
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.contactNumber)
        ops.add(builder.build())

        // Email Address
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(ContactsContract.CommonDataKinds.Email.DATA, contact.contactEmailId)
        builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, 2)
        ops.add(builder.build())

        // Address
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            contact.contactAddress
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
            contact.contactCountry
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
            contact.contactPostalCode
        )
        ops.add(builder.build())


        //Organization details
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.Organization.COMPANY,
            contact.contactOrganization
        )
        builder.withValue(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.Organization.TITLE,
            contact.contactJobTitle
        )
        builder.withValue(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
        )
        ops.add(builder.build())

//        Contact Image
        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
        builder.withSelection(
            ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
            arrayOf(
                contactId,
                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
            )
        )
        builder.withValue(
            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
            contact.contactImage
        )
        ops.add(builder.build())

        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } catch (e: OperationApplicationException) {
            e.printStackTrace()
        }
    }

    /*
                   Search Contact By Id
    */

    suspend fun searchContactById(
        context: Context,
        contactId: String
    ): Contact {

        var contact = Contact(contactId, "", "", "", "", "", "", "", "", "", "", "")

        getContactInfo(
            context,
            contact,
            contactId
        )

        getEmailId(
            context,
            contact,
            contactId
        )

        getAddress(
            context,
            contact,
            contactId
        )

        getContactOrganization(
            context,
            contact,
            contactId
        )
        getContactWebsite(
            context,
            contact,
            contactId
        )
        return contact
    }

    suspend fun searchContactByNumber(
        context: Context,
        contactNumber: String
    ): Contact {

        val where = "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?"
        val param = arrayOf<String>(
            contactNumber
        )
        val contentResolver = context.contentResolver
        val contact = Contact("", "", "", "", "", "", "", "", "", "", "", "")
        var contactId = ""
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                mColumnProjections,
                where,
                param,
                null
            )

            cursor?.let { cursor ->
                while (cursor.moveToNext()) {
                    contactId = cursor.getString(0)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            Log.e(PROVIDER_ERROR, "searchContactByNumber: $e")
        } finally {
            if (cursor != null && !cursor.isClosed)
                cursor.close()
        }


        contact.contactId = contactId
        return contact
    }


    private fun getContactInfo(context: Context, contact: Contact, contactId: String) {
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            mColumnProjections,
            whereClause,
            arrayOf<String>(contactId),
            null
        )

        var contactName: String? = ""
        var contactPhotoUri: String? = ""
        var contactThumbnailPhotoUri: String? = ""
        var contactNumber: String? = ""
        if (cursor != null && cursor.count > 0) {
            try {
                while (cursor.moveToNext()) {
                    contactName = cursor.getString(1) ?: ""
                    contactNumber = cursor.getString(2) ?: ""
                    contactPhotoUri = cursor.getString(3) ?: ""
                    contactThumbnailPhotoUri = cursor.getString(4) ?: ""

                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "getContactInfo: $e")
            } finally {
                cursor.close()
            }
        }

        if (contactName != null) {
            contact.contactName = contactName.toString()
        }


        if (contactNumber != null) {
            contact.contactNumber = contactNumber.toString()
        }


        if (contactThumbnailPhotoUri != null) {
            contact.contactThumbnailImage = contactThumbnailPhotoUri.toString()
        }


        if (contactPhotoUri != null) {
            contact.contactImage = contactPhotoUri.toString()
        }


//        if (contactOrganization != null) {
//            contact.contactOrganization = contactOrganization.toString()
//        }
//
//
//        if (contactJobTitle != null) {
//            contact.contactJobTitle = contactJobTitle.toString()
//        }
//
//
//        if (contactWebsite != null) {
//            contact.contactWebAddress = contactWebsite.toString()
//        }

    }

    private fun getContactOrganization(context: Context, contact: Contact, contactId: String) {
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"

        val projections = arrayOf<String>(
            ContactsContract.CommonDataKinds.Organization.COMPANY,
            ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
        )
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projections,
            whereClause,
            arrayOf<String>(contactId),
            null
        )


        var contactOrganization: String? = ""
        var contactJobTitle: String? = ""
        if (cursor != null && cursor.count > 0) {
            try {
                while (cursor.moveToNext()) {
                    contactOrganization = cursor.getString(0) ?: ""
                    contactJobTitle = cursor.getString(1) ?: ""

                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "getContactInfo: $e")
            } finally {
                cursor.close()
            }
        }




        if (contactOrganization != null) {
            contact.contactOrganization = contactOrganization.toString()
        }


        if (contactJobTitle != null) {
            contact.contactJobTitle = contactJobTitle.toString()
        }


    }

    private fun getContactWebsite(context: Context, contact: Contact, contactId: String) {
        val whereClause =
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"

        val projections = arrayOf<String>(
            ContactsContract.CommonDataKinds.Website.URL,
        )
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projections,
            whereClause,
            arrayOf<String>(contactId),
            null
        )


        var contactWebsite: String? = ""
        if (cursor != null && cursor.count > 0) {
            try {
                while (cursor.moveToNext()) {
                    contactWebsite = cursor.getString(0) ?: ""
                }
            } catch (e: Exception) {
                Log.e("EXCEPTION", "getContactInfo: $e")
            } finally {
                cursor.close()
            }
        }


        if (contactWebsite != null) {
            contact.contactWebAddress = contactWebsite.toString()
        }

    }



    @SuppressLint("Range")
    override suspend fun deleteContact(context: Context, contact: Contact): Boolean {
        val contactUri: Uri =
            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contact.contactNumber))
        val cur: Cursor? = context
            .getContentResolver()
            .query(contactUri, null, null, null, null)

        try {
            cur?.let {
                if (cur.moveToFirst()) {
                    do {
                        if (cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME))
                                .equals(contact.contactName, ignoreCase = true)
                        ) {
                            val lookupKey =
                                cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                            val uri: Uri = Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                                lookupKey
                            )
                            context.getContentResolver().delete(uri, null, null)
                            return true
                        }
                    } while (cur.moveToNext())
                }
                cur.close()
            }
        } catch (e: Exception) {
            println(e.stackTrace)
        }finally {
            cur?.let {
                cur.close()
            }
        }
        return false
    }

    override suspend fun deleteContactList(context: Context, contacts: List<Contact>): Boolean {
        val contactIterator = contacts.iterator()
        while (contactIterator.hasNext()){
            val contact = contactIterator.next()
            val result = deleteContact(context,contact)
            if (!result) return false;
        }
        return true
    }


}