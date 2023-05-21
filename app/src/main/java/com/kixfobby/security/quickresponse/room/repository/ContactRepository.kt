package com.kixfobby.security.quickresponse.room.repository

import androidx.lifecycle.LiveData
import com.kixfobby.security.quickresponse.room.dao.ContactDao
import com.kixfobby.security.quickresponse.room.entity.ContactEntity

class ContactRepository(private var contactDao: ContactDao) {
    val getAllContacts: LiveData<List<ContactEntity>> = contactDao.getAllContactsDao()

    suspend fun addContact(contactEntity: ContactEntity) {
        contactDao.insertContact(contactEntity)
    }

    suspend fun deleteContact(contactName: String, contactNumber: String) {
        contactDao.deleteContact(contactName, contactNumber)
    }

    suspend fun deleteAllContacts(contactEntity: ContactEntity) {
        contactDao.deleteAllContacts(contactEntity)
    }

    suspend fun isContactExists(contactName: String, contactNumber: String): Boolean =
        contactDao.isContactExists(contactName, contactNumber)

}