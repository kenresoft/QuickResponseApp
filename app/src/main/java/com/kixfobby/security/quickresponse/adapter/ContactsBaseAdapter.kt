package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.ContactBase
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.room.viewmodel.ContactViewModel
import com.kixfobby.security.quickresponse.storage.ContactManager.removePerson
import com.kixfobby.security.quickresponse.storage.ContactManager.savePerson
import com.kixfobby.security.quickresponse.storage.Pref
import java.io.FileNotFoundException
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class ContactsBaseAdapter(private val ctx: Context, val data: ArrayList<ContactBase>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val registeredUsersUid: MutableList<String?>
    private val registeredUsersName: MutableList<String?>
    private val registeredUsersPhone: MutableList<String?>
    private var mOnItemClickListener: OnItemClickListener? = null
    private val contactsBaseAdapter: ContactsBaseAdapter? = null
    private val databaseReference: DatabaseReference
    private val reference1: DatabaseReference
    private val mAuth: FirebaseAuth
    private val mAuthListener: AuthStateListener? = null
    private val user: FirebaseUser? = null
    private val user1: FirebaseUser?
    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        vh = OriginalViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val view = holder
            val contact = data[position]
            val phone: String? = Pref(ctx).get("phone", "phone")

            val userList = retrieveUsers(ctx)
            val phone1 = contact.number.trim { it <= ' ' }
            for (use: User in userList) {
                if ((use.phone!!.trim { it <= ' ' } == phone1)) {
                    val b = true
                    val s = "Registered"
                    view.register.text = s
                    //view.active.setText(p.getName());
                    if (use.uid != user1!!.uid || use.phone != phone || !use.phone.equals(contact.number)) {
                        registeredUsersUid.add(use.uid)
                        registeredUsersName.add(use.name)
                        registeredUsersPhone.add(use.phone)
                    }
                }
            }
            val uidSet: MutableSet<String?> = HashSet()
            uidSet.addAll(registeredUsersUid)
            Pref(ctx).put("contacts_uid_set", uidSet)

            val nameSet: MutableSet<String?> = HashSet()
            nameSet.addAll(registeredUsersName)
            Pref(ctx).put("contacts_name_set", nameSet)

            val phoneSet: MutableSet<String?> = HashSet()
            phoneSet.addAll(registeredUsersPhone)
            Pref(ctx).put("contacts_phone_set", phoneSet)

            view.name.text = contact.name
            view.number.text = contact.number

            /*view.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContactManager.removePerson(ctx, contact);
                    items.remove(position);
                    ContactsBaseAdapter.this.notifyDataSetChanged();
                }
            });*/view.lyt_parent.setOnClickListener(View.OnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, contact, position)
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun removeItem(position: Int) {
        removePerson(ctx, data[position])
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: ContactBase, position: Int) {
        data.add(position, item)
        savePerson(ctx, item)
        notifyItemInserted(position)
    }

    fun retrieveUsers(c: Context): List<User> {
        var ret: List<User> = ArrayList()
        try {
            val fis = c.openFileInput("users_db")
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<User>
        } catch (e: FileNotFoundException) {
            saveUsers(ctx, ret)
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
        } catch (e: ClassNotFoundException) {
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
            e.printStackTrace()
        }
        return ret
    }

    fun saveUsers(c: Context, user: List<User>?) {
        try {
            val fos = c.openFileOutput("users_db", Context.MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(user)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE NOT FOUND", Toast.LENGTH_SHORT).show();
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: ContactBase?, position: Int)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var lyt_parent: View
        var name: TextView
        var number: TextView
        var register: TextView
        var active: TextView
        var btnRemove: ImageButton

        init {
            name = v.findViewById(R.id.contact_item_name)
            number = v.findViewById(R.id.contact_item_number)
            register = v.findViewById(R.id.item_registered)
            active = v.findViewById(R.id.item_active)
            btnRemove = v.findViewById(R.id.sms_item_remove)
            lyt_parent = v.findViewById(R.id.lyt_parent)
        }
    }

    companion object {
        private val reg: String? = null
    }

    init {
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = databaseReference.child(user1!!.uid)
        registeredUsersUid = ArrayList()
        registeredUsersName = ArrayList()
        registeredUsersPhone = ArrayList()
    }
}