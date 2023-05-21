package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.ContactAdapter
import com.kixfobby.security.quickresponse.helper.SwipeToDeleteCallback
import com.kixfobby.security.quickresponse.model.ContactBase
import com.kixfobby.security.quickresponse.model.ContactRetriever
import com.kixfobby.security.quickresponse.room.entity.ContactEntity
import com.kixfobby.security.quickresponse.room.viewmodel.ContactViewModel
import com.kixfobby.security.quickresponse.storage.Constants.RESULT_PICK_CONTACT1
import com.kixfobby.security.quickresponse.storage.ContactManager.getSavedPersons
import com.kixfobby.security.quickresponse.storage.Pref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ContactsBaseActivity() : BaseActivity() {
    private var items: ArrayList<ContactBase>? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var recyclerView: RecyclerView? = null
    private var contactsBaseAdapter: ContactAdapter? = null
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private val user: FirebaseUser? = null
    private var user1: FirebaseUser? = null
    private var uid1: String? = null
    private lateinit var contactViewModel: ContactViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        reference1 = databaseReference!!.child(user1!!.uid)
        initUi()
        supportActionBar?.setTitle(R.string.security_contacts)
    }

    override fun onResume() {
        super.onResume()
        contactsBaseAdapter!!.notifyDataSetChanged()

        //Toast.makeText(getBaseContext(), "Resumed", Toast.LENGTH_SHORT).show();
    }

    private fun initUi() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        items = getSavedPersons(this)
        //contactsBaseAdapter = ContactsBaseAdapter(this, items!!)

        contactsBaseAdapter = ContactAdapter()
        recyclerView = findViewById(R.id.contacts_list)
        recyclerView!!.setLayoutManager(LinearLayoutManager(this))
        //recyclerView.addItemDecoration(new LinearItemDecoration(getBaseContext(), LinearItemDecoration.VERTICAL_LIST));
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.setNestedScrollingEnabled(false)
        recyclerView!!.setAdapter(contactsBaseAdapter)
        contactsBaseAdapter!!.notifyDataSetChanged()

        contactViewModel = ViewModelProvider(this).get(ContactViewModel::class.java)
        contactViewModel.getAllContacts.observe(this, Observer { contact -> contactsBaseAdapter!!.setContact(contact) })

        enableSwipeToDeleteAndUndo()

        val uid = user1!!.uid
        val name = user1!!.displayName
        val phone = user1!!.phoneNumber

        if (contactsBaseAdapter!!.itemCount == 0) {
            Toast.makeText(baseContext, "No Contacts Added", Toast.LENGTH_SHORT).show()
        }

        /*contactsBaseAdapter!!.setOnItemClickListener(object : ContactAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: ContactBase?, position: Int) {
                //val it = UpdateService().retrieveUsers(this@ContactsBaseActivity)
                //val user = User().setName(name).setPhone(phone).setUid(uid)
                val ph1 = obj?.number?.trim { it <= ' ' }
                val intent = Intent(this@ContactsBaseActivity, ChatRoomActivity::class.java)
                intent.putExtra("thisUser", user)
                for (u: User in it) {
                    if ((u.phone!!.trim { it <= ' ' } == ph1)) {
                        obj.setUid((u.uid)!!)
                        uid1 = obj.getUid()
                        try {
                            intent.putExtra("thisFriend", uid1)
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                            intent.putExtra("thisFriend", "uid1")
                        }
                    }
                }
                FBaseUtil.saveUser(user, this@ContactsBaseActivity)
                startActivity(intent)
            }
        })*/
        //showNotifAlert();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "Pick Contact").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_contact_page_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        //menu.add(Menu.NONE, 1, Menu.NONE, "Create Contact").setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.baseline_import_contacts_24)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                chooseContact()
                /*contactsBaseAdapter.notifyDataSetChanged();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);*/return true
            }
            1 -> //refresh();
                return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun chooseContact() {
        val i = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(i, RESULT_PICK_CONTACT1)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_PICK_CONTACT1) {
                val phone = Pref(this).get("phone", "phone")
                val contractData = data!!.data
                val cr = ContactRetriever(applicationContext, (contractData)!!)
                val p = cr.person
                if (p == null) {
                    toast(this, "Phone number not found!")
                } else {
                    if ((p.number == phone)) {
                        toast(this, "Sorry, you can't add yourself")
                    } else if (getSavedPersons(this).size >= 5) {
                        toast(this, "Sorry, you can add only 5 contacts!")
                    } else {
                        //savePerson(this@ContactsBaseActivity, p)

                        GlobalScope.launch(Dispatchers.IO) {
                            if (!contactViewModel.isContactExists(p.name!!, p.number)) contactViewModel.addContact(ContactEntity(0, p.name!!, p.number, "0"))
                        }

                        contactsBaseAdapter!!.notifyDataSetChanged()
                        reference1!!.child("Security Contacts").push().setValue(p)
                        refresh()
                    }
                }
            }
        }
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                //val item = contactsBaseAdapter!!.data[position]
                //contactsBaseAdapter!!.removeItem(position)

                val name = viewHolder.itemView.findViewById<TextView>(R.id.contact_item_name).text.toString()
                val number = viewHolder.itemView.findViewById<TextView>(R.id.contact_item_number).text.toString()

                contactViewModel.deleteContact(name, number)
                val snackbar = Snackbar.make(
                    (coordinatorLayout)!!, "Contact removed successfully.", Snackbar.LENGTH_LONG
                )
                snackbar.setAction("UNDO", object : View.OnClickListener {
                    override fun onClick(view: View) {
                        //contactsBaseAdapter!!.restoreItem(item, position)
                        //contactsBaseAdapter!!.restoreContact()
                        GlobalScope.launch(Dispatchers.IO) {
                            if (!contactViewModel.isContactExists(name, number)) contactViewModel.addContact(
                                ContactEntity(0, name, number, "0")
                            )
                        }

                        recyclerView!!.scrollToPosition(position)
                    }
                })
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }
}