package com.kixfobby.security.quickresponse.model

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log

class ContactRetriever(private val context: Context, contractData: Uri) {
    private val TAG = "ContactRetriever"
    private val cr: ContentResolver = context.contentResolver
    private val contractData: Uri = contractData
    private var id: String? = null

    val person: ContactBase?
        get() {
            val name = name
            val num = number
            return if (name != null && num != null) ContactBase(number, name) else null
        }

    private val number: String
        @SuppressLint("Range") private get() {
            var ret: String? = null
            val cId = cr.query(contractData, arrayOf(ContactsContract.Contacts._ID), null, null, null)
            if (cId!!.moveToFirst()) {
                id = cId.getString(cId.getColumnIndex(ContactsContract.Contacts._ID))
                //Log.i("$TAG IDs: ", id)
            }
            cId.close()
            val cNum = cr.query(contractData, null, null, null, null)
            if (cNum!!.moveToNext()) {
                ret = cNum.getString(cNum.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                Log.i(TAG , ret)
            }
            return trims(ret)
        }

    private val name: String?
        @SuppressLint("Range")
        get() {
            var ret: String? = null
            val c = cr.query(contractData, null, null, null, null)
            if (c!!.moveToFirst()) ret = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            c.close()
            Log.i(TAG + "NAMES: ", ret!!)
            return ret
        }

    fun trims(str: String?): String {
        return str!!.replace("\\s".toRegex(), "")
    }

}