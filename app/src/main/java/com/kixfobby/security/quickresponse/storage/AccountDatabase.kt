package com.kixfobby.security.quickresponse.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AccountDatabase(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(detail: SQLiteDatabase) {
        val details = ("CREATE TABLE " + TABLE_NAME + "(email TEXT NOT NULL, pin LONG NOT NULL, phone LONG NOT NULL,"
                + "country TEXT NOT NULL, state TEXT NOT NULL, zip TEXT NOT NULL,"
                + "kin1 TEXT NOT NULL, kin2 TEXT NOT NULL, kin3 TEXT NOT NULL,"
                + "num1 LONG NOT NULL,num2 LONG NOT NULL, num3 LONG NOT NULL);")
        detail.execSQL(details)
    }

    override fun onUpgrade(detail: SQLiteDatabase, i: Int, i1: Int) {
        detail.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(detail)
    }

    fun createData(
        email: String?,
        pin: Long,
        phone: Long,
        country: String?,
        state: String?,
        zip: String?,
        kin1: String?,
        kin2: String?,
        kin3: String?,
        num1: Long,
        num2: Long,
        num3: Long
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("email", email)
        values.put("pin", pin)
        values.put("phone", phone)
        values.put("country", country)
        values.put("state", state)
        values.put("zip", zip)
        values.put("kin1", kin1)
        values.put("kin2", kin2)
        values.put("kin3", kin3)
        values.put("num1", num1)
        values.put("num2", num2)
        values.put("num3", num3)
        val insertion = db.insert(TABLE_NAME, null, values)
        db.close()
        return if (insertion == -1L) false else true
    }

    fun getData(email: String): Cursor {
        val db = this.writableDatabase
        val query = "SELECT * FROM " + TABLE_NAME + " WHERE email='" + email + "'"
        return db.rawQuery(query, null)
    }

    fun updateData(
        email: String,
        pin: Long,
        phone: Long,
        country: String?,
        state: String?,
        zip: String?,
        kin1: String?,
        kin2: String?,
        kin3: String?,
        num1: Long,
        num2: Long,
        num3: Long
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("email", email)
        values.put("pin", pin)
        values.put("phone", phone)
        values.put("country", country)
        values.put("state", state)
        values.put("zip", zip)
        values.put("kin1", kin1)
        values.put("kin2", kin2)
        values.put("kin3", kin3)
        values.put("num1", num1)
        values.put("num2", num2)
        values.put("num3", num3)
        db.update(TABLE_NAME, values, "email=?", arrayOf(email))
        return true
    }

    fun deleteData(email: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME, "email = ?", arrayOf(email))
    }

    val allData: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)
        }

    companion object {
        const val COLUMN_ID = "id"
        private const val DATABASE_NAME = "account.db"
        private const val TABLE_NAME = "account_table"
        private const val DATABASE_VERSION = 1
    }
}