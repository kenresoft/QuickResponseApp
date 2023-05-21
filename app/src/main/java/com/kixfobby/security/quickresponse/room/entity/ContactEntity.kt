package com.kixfobby.security.quickresponse.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_table")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var number: String,
    var uid: String
)