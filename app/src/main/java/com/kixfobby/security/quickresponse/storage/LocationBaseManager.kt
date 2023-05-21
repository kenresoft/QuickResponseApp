package com.kixfobby.security.quickresponse.storage

import android.content.Context
import android.util.Log
import com.kixfobby.security.quickresponse.model.LocationBase
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

object LocationBaseManager {
    private val SAVEFILE = LocationBaseManager::class.java.name

    @JvmStatic
    fun saveLocation(c: Context, l: LocationBase) {
        val baseLocation = getSavedLocation(c)
        for (lb in baseLocation) {
            if (lb.equals(l)) {
                return
            }
        }
        baseLocation.add(l)
        Log.i("PERSON MANAGER", "Added: " + l.location)
        setSavedLocation(c, baseLocation)
    }

    @JvmStatic
    fun removeLocation(c: Context, l: LocationBase) {
        val baseLocation = getSavedLocation(c)
        for (i in baseLocation.indices) {
            if (i < baseLocation.lastIndex + 1) {
                if (baseLocation[i].equals(l)) {
                    baseLocation.removeAt(i)
                    Log.i("PERSON MANAGER", "Removed: " + l.location)
                }
            }
        }
        setSavedLocation(c, baseLocation)
    }

    private fun setSavedLocation(c: Context, baseLocation: ArrayList<LocationBase>) {
        try {
            val fos = c.openFileOutput(SAVEFILE, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(baseLocation)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE NOT FOUND", Toast.LENGTH_SHORT).show();
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
        }
    }

    @JvmStatic
    fun getSavedLocation(c: Context): ArrayList<LocationBase> {
        var ret = ArrayList<LocationBase>()
        try {
            val fis = c.openFileInput(SAVEFILE)
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<LocationBase>
        } catch (e: FileNotFoundException) {
            setSavedLocation(c, ArrayList())
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
        } catch (e: ClassNotFoundException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
            e.printStackTrace()
        }
        Collections.sort(ret) { o1, o2 -> if (o1.time == null || o2.time == null) 0 else o2.time.compareTo(o1.time) }
        return ret
    }
}