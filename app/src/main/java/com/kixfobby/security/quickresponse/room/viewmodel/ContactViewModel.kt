package com.kixfobby.security.quickresponse.room.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kixfobby.security.quickresponse.room.di.AppComponents
import com.kixfobby.security.quickresponse.room.entity.ContactEntity
import com.kixfobby.security.quickresponse.room.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val getAllContacts: LiveData<List<ContactEntity>>

    init {
        val contactDao = AppComponents.provideAppDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        getAllContacts = repository.getAllContacts
    }

    fun addContact(contactEntity: ContactEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.addContact(contactEntity)
    }

    fun deleteContact(contactName: String, contactNumber: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteContact(contactName, contactNumber)
    }

    fun deleteAllContacts(contactEntity: ContactEntity) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAllContacts(contactEntity)
    }

    suspend fun isContactExists(contactName: String, contactNumber: String): Boolean {
        return repository.isContactExists(contactName, contactNumber)
    }

}