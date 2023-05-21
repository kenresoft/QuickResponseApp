package com.kixfobby.security.quickresponse.storage

import android.content.Context
import android.util.Log
import com.kixfobby.security.quickresponse.model.SmsBase
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

object SmsBaseManager {
    private val SAVEFILE = SmsBaseManager::class.java.name
    fun saveSms(p: SmsBase, c: Context) {
        val sms = getSavedSms(c)
        for (s in sms) {
            if (s.equals(p)) {
                return
            }
        }
        sms.add(p)
        Log.i("PERSON MANAGER", "Added: " + p.message)
        setSavedSms(sms, c)
    }

    @JvmStatic
    fun removeSms(c: Context, p: SmsBase) {
        val sms = getSavedSms(c)
        for (i in sms.indices) {
            if (i < sms.lastIndex + 1) {
                if (sms[i].equals(p)) {
                    sms.removeAt(i)
                    Log.i("PERSON MANAGER", "Removed: " + p.message)
                }
            }
        }
        setSavedSms(sms, c)
    }

    private fun setSavedSms(sms: ArrayList<SmsBase>, c: Context) {
        try {
            val fos = c.openFileOutput(SAVEFILE, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(sms)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE NOT FOUND", Toast.LENGTH_SHORT).show();
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
        }
    }

    @JvmStatic
    fun getSavedSms(c: Context): ArrayList<SmsBase> {
        var ret = ArrayList<SmsBase>()
        try {
            val fis = c.openFileInput(SAVEFILE)
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<SmsBase>
        } catch (e: FileNotFoundException) {
            setSavedSms(ArrayList(), c)
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
        } catch (e: ClassNotFoundException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
            e.printStackTrace()
        }

        /*//File[] files = file.listFiles();
        Arrays.sort(ret, new Comparator<ArrayList>() {
            @Override
            public int compare(ArrayList f1, ArrayList f2) {
                if (f1.get(1) - f2.lastModified() == 0) {
                    return 0;
                } else {
                    return f1.lastModified() - f2.lastModified() > 0 ? -1 : 1;
                }
            }
        });*/
        ret.sortWith { o1, o2 -> if (o1.dt == null || o2.dt == null) 0 else o2.dt.compareTo(o1.dt) }
        return ret
    }
}