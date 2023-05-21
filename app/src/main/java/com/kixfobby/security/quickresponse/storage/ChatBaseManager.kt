package com.kixfobby.security.quickresponse.storage

import android.content.Context
import android.util.Log
import com.kixfobby.security.quickresponse.model.ChatBase
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

object ChatBaseManager {
    private val SAVEFILE = ChatBaseManager::class.java.name
    fun saveChat(p: ChatBase, c: Context) {
        val chat = getSavedChat(c)
        for (i in chat.indices) {
            if (i < chat.lastIndex + 1) {
                if (chat[i].equals(p)) {
                    return
                }
                if (chat[i].name == "Contact Name") {
                    chat.removeAt(i)
                }
            }
        }
        chat.add(p)
        Log.i("PERSON MANAGER", "Added: " + p.message)
        setSavedChat(chat, c)
    }

    @JvmStatic
    fun removeChat(c: Context, p: ChatBase) {
        val chat = getSavedChat(c)
        for (i in chat.indices) {
            if (i < chat.lastIndex + 1) {
                if (chat[i].equals(p)) {
                    chat.removeAt(i)
                    Log.i("PERSON MANAGER", "Removed: " + p.message)
                }
            }
        }
        setSavedChat(chat, c)
    }

    private fun setSavedChat(chat: ArrayList<ChatBase>, c: Context) {
        try {
            val fos = c.openFileOutput(SAVEFILE, Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(chat)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE NOT FOUND", Toast.LENGTH_SHORT).show();
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE CONTACTS FILE", Toast.LENGTH_SHORT).show();
        }
    }

    @JvmStatic
    fun getSavedChat(c: Context): ArrayList<ChatBase> {
        var ret = ArrayList<ChatBase>()
        try {
            val fis = c.openFileInput(SAVEFILE)
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<ChatBase>
        } catch (e: FileNotFoundException) {
            setSavedChat(ArrayList(), c)
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