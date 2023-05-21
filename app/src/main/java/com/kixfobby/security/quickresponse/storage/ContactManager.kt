package com.kixfobby.security.quickresponse.storage

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.kixfobby.security.quickresponse.model.ContactBase
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

@SuppressLint("LogNotTimber")
object ContactManager {
    private val SAVEFILE = ContactManager::class.java.name
    private var phone: String? = null

    @JvmStatic
    fun savePerson(c: Context, p: ContactBase) {
        val persons = getSavedPersons(c)
        for (per in persons) {
            if (per.equals(p)) {
                return
            }
        }
        persons.add(p)
        Log.i("PERSON MANAGER", "Added: " + p.name)
        setSavedPersons(c, persons)
    }

    @JvmStatic
    fun removePerson(c: Context, p: ContactBase) {
        val persons = getSavedPersons(c)
        for (i in persons.indices) {
            if (i < persons.lastIndex + 1) {
                if (persons[i].equals(p)) {
                    persons.removeAt(i)
                    Log.i("PERSON MANAGER", "Removed: " + p.name)
                    //Toast.makeText(c, "$i....${persons.size}.....$persons[i].....$p", Toast.LENGTH_LONG).show()
                }
                //else persons.remove(i);
                //Toast.makeText(c, "$i....${persons.size}.....$persons[i].....$p", Toast.LENGTH_SHORT).show()
            }
        }
        setSavedPersons(c, persons)
    }

    private fun setSavedPersons(c: Context, persons: ArrayList<ContactBase>) {
        phone = Pref(c).get("phone", "phone")
        try {
            for (i in persons.indices) {
                if (i < persons.lastIndex) {
                    if (persons[i].number == phone && persons[i + 1].number != phone) {
                        persons.removeAt(i)
                    }
                }
            }
            val fos = c.openFileOutput(SAVEFILE, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(persons)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE NOT FOUND", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun getSavedPersons(c: Context): ArrayList<ContactBase> {
        var ret = ArrayList<ContactBase>()
        phone = Pref(c).get("phone", "phone")
        try {
            val fis = c.openFileInput(SAVEFILE)
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<ContactBase>
            for (i in ret.indices) {
                if (i < ret.lastIndex) {
                    if (ret[i].number == phone && ret[i + 1].number != phone) {
                        ret.removeAt(i)
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            setSavedPersons(c, ArrayList())
        } catch (e: IOException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
        } catch (e: ClassNotFoundException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        ret.sortWith { o2, o1 -> if (o1.name == null || o2.name == null) 0 else o2.name!!.compareTo(o1.name!!) }
        return ret
    }
}