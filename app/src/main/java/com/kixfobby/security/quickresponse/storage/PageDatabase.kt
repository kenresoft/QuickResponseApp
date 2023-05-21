package com.kixfobby.security.quickresponse.storage

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class PageDatabase(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_USERS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + PAGES_TABLE)
        onCreate(db)
    }

    fun addPage(id: String?, page: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ID, id)
        values.put(COLUMN_PAGE, page)
        val result = db.insert(PAGES_TABLE, page, values)
        db.close()
        Log.d(TAG, "Page Added$result")
        return result != -1L
    }

    fun updatePage(page: String?) {
        val db = this.writableDatabase
        val values = ContentValues()
        //values.put(COLUMN_PAGE, id);
        values.put(COLUMN_PAGE, page)
        val result = db.update(PAGES_TABLE, values, "id=?", arrayOf(COLUMN_ID)).toLong()
        db.close()
        Log.d(TAG, "Page Updated$result")
    }

    val allPages: Cursor
        get() {
            val db = this.writableDatabase
            return db.rawQuery("SELECT * FROM $PAGES_TABLE", null)
        }

    companion object {
        val TAG = PageDatabase::class.java.simpleName
        const val DB_NAME = "pages.db"
        const val PAGES_TABLE = "pages_table"
        const val DB_VERSION = 1
        const val COLUMN_ID = "id"
        const val COLUMN_PAGE = "page"
        const val CREATE_TABLE_USERS = ("CREATE TABLE " + PAGES_TABLE + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_PAGE + " TEXT" + ");")
    }
}