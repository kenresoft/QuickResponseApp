package com.kixfobby.security.quickresponse.storage

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.kixfobby.security.quickresponse.model.ProfileBase
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

object ProfileManager {
    private val SAVEFILE = ProfileManager::class.java.name
    fun saveInfo(c: Context, p: ProfileBase) {
        val profile = getSavedInfo(c)
        clearAllInfo(c, p)
        for (per in profile) {
            if (per == p) {
                return
            }
        }
        profile.add(0, p)
        Log.i("PERSON MANAGER", "Added: ")
        setSavedInfo(c, profile)
    }

    fun removeInfo(c: Context, p: ProfileBase) {
        val profile = getSavedInfo(c)
        for (i in profile.indices) {
            if (i < profile.lastIndex + 1) {
                if (profile[i].equals(p)) {
                    profile.removeAt(i)
                    Log.i("PERSON MANAGER", "Removed: ")
                }
            }
        }
        setSavedInfo(c, profile)
    }

    fun clearAllInfo(c: Context, p: ProfileBase) {
        val profile = getSavedInfo(c)
        for (i in profile.indices) {
            if (profile[i] == p) {
                profile.clear()
                Log.i("PERSON MANAGER", "Removed: ")
            }
        }
        setSavedInfo(c, profile)
    }

    fun setSavedInfo(c: Context, profile: ArrayList<ProfileBase>?) {
        try {
            val fos = c.openFileOutput(SAVEFILE, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(profile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE NOT FOUND", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
        }
    }

    fun getSavedInfo(c: Context): ArrayList<ProfileBase> {
        var ret = ArrayList<ProfileBase>()
        try {
            val fis = c.openFileInput(SAVEFILE)
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<ProfileBase>
        } catch (e: FileNotFoundException) {
            setSavedInfo(c, ArrayList())
        } catch (e: IOException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
        } catch (e: ClassNotFoundException) {
            Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        return ret
    }
}