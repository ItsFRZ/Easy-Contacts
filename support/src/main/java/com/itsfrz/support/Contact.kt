package com.itsfrz.support

import android.content.ContentProviderOperation
import android.provider.ContactsContract

data class Contact(
    var contactId : String,
    var contactName : String,
    var contactNumber : String,
    var contactImage : String,
    var contactThumbnailImage : String,
    var contactOrganization : String,
    var contactJobTitle : String,
    var contactAddress : String,
    var contactWebAddress : String,
    var contactEmailId : String,
    var contactCountry : String,
    var contactPostalCode : String,
)



