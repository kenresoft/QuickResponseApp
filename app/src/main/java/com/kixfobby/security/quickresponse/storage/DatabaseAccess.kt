package com.kixfobby.security.quickresponse.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.util.*

class DatabaseAccess private constructor(context: Context) {
    private val openHelper: SQLiteOpenHelper
    private var database: SQLiteDatabase? = null

    fun open() {
        database = openHelper.writableDatabase
    }

    fun close() {
        if (database != null) {
            database!!.close()
        }

    }

    val names: List<String>
        get() {
            open()
            val list: MutableList<String> = ArrayList()
            val cursor = database!!.rawQuery("SELECT country FROM codes", null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(cursor.getString(0))
                cursor.moveToNext()
            }
            cursor.close()
            close()
            return list
        }

    fun getPoliceCode(country: String): String? {
        open()
        var police: String? = null
        val cursor = database!!.rawQuery("SELECT * FROM codes WHERE country = ?", arrayOf(country))
        while (cursor.moveToNext()) {
            police = cursor.getString(2)
            break
        }
        cursor.close()
        close()
        return police
    }

    fun getFireCode(country: String): String? {
        open()
        var fire: String? = null
        val cursor = database!!.rawQuery("SELECT * FROM codes WHERE country = ?", arrayOf(country))
        while (cursor.moveToNext()) {
            fire = cursor.getString(3)
            break
        }
        cursor.close()
        close()
        return fire
    }

    val tipTitle: ArrayList<String>
        get() {
            open()
            val list = ArrayList<String>()
            val cursor = database!!.rawQuery("SELECT title FROM tips", null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                list.add(cursor.getString(0))
                cursor.moveToNext()
            }
            cursor.close()
            close()
            return list
        }

    fun getTipDetail(title: String): String? {
        open()
        var detail: String? = null
        val cursor = database!!.rawQuery("SELECT * FROM tips WHERE title = ?", arrayOf(title))
        while (cursor.moveToNext()) {
            detail = cursor.getString(2)
            break
        }
        cursor.close()
        close()
        return detail
    }

/*    fun getTipPhotos(context: Context): ArrayList<Drawable>{
            open()
            val list = ArrayList<Drawable>()
            val cursor = database!!.rawQuery("SELECT photo FROM tips", null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                var photo: ByteArray = cursor.getBlob(0)
                var image:Drawable = BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(photo, 0, photo.size))
                list.add(image)
                cursor.moveToNext()
            }
            cursor.close()
            close()
            return list
        }

    fun getTipPhoto(title: String): ByteArray? {
        open()
        var photo: ByteArray? = null
        val cursor = database!!.rawQuery("SELECT photo FROM tips WHERE title = ?", arrayOf(title))
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            photo = cursor.getBlob(0)
            break
        }
        cursor.close()
        close()
        return photo
    }*/

    companion object {
        private var instance: DatabaseAccess? = null

        /**
         * Return a singleton instance of DatabaseAccess.
         *
         * @param context the Context
         * @return the instance of DatabaseAccess
         */
        @JvmStatic
        fun getInstance(context: Context): DatabaseAccess? {
            if (instance == null) {
                instance = DatabaseAccess(context)
            }
            return instance
        }
    }

    /**
     * Private constructor to avoid object creation from outside classes.
     *
     * @param context
     */
    init {
        openHelper = DatabaseOpenHelper(context)
    }
}