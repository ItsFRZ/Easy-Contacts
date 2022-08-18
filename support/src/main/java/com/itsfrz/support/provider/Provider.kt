package com.itsfrz.support.provider

import android.content.*
import android.database.Cursor
import android.os.RemoteException
import android.provider.ContactsContract
import android.util.Log
import com.itsfrz.support.Contact


object ContactProvider : ProviderService {

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
        cursor
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
        val ops = arrayListOf<ContentProviderOperation>()
        val rawContactInsertIndex = ops.size;

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


    override suspend fun updateContact(context: Context, contact: Contact) {

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
}