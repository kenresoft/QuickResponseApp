package com.kixfobby.security.quickresponse.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.kixfobby.security.quickresponse.room.entity.ContactEntity

@Dao
interface ContactDao {

    @Query("SELECT * FROM contact_table ORDER BY id ASC")
    fun getAllContactsDao(): LiveData<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contactBase: ContactEntity)

    @Query("DELETE FROM contact_table WHERE name = :contactName and number = :contactNumber")
    suspend fun deleteContact(contactName: String, contactNumber: String)

    @Delete
    suspend fun deleteAllContacts(contactBase: ContactEntity)

    @Query("SELECT EXISTS(SELECT * FROM contact_table WHERE name = :contactName or number = :contactNumber)")
    suspend fun isContactExists(contactName: String, contactNumber: String) : Boolean
}