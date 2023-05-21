package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.FirestoreAdapter


class UsersActivity : BaseActivity() {
    private var items: ArrayList<String>? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var recyclerView: RecyclerView? = null
    private var firestoreAdapter: FirestoreAdapter? = null
    private var mFirestore: FirebaseFirestore? = null


    private var uid1: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        mFirestore = FirebaseFirestore.getInstance()
        initUi()
        supportActionBar!!.setTitle(R.string.security_contacts)
    }

    /*override fun onResume() {
        super.onResume()
        firestoreAdapter!!.notifyDataSetChanged()

        //Toast.makeText(getBaseContext(), "Resumed", Toast.LENGTH_SHORT).show();
    }*/

    private fun initUi() {
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        items = readDataFromFirestore()
        firestoreAdapter = FirestoreAdapter(items!!, this)
        recyclerView = findViewById(R.id.contacts_list)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //recyclerView.addItemDecoration(new LinearItemDecoration(getBaseContext(), LinearItemDecoration.VERTICAL_LIST));
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.isNestedScrollingEnabled = false
        recyclerView!!.adapter = firestoreAdapter
        firestoreAdapter!!.notifyDataSetChanged()
        //enableSwipeToDeleteAndUndo()
        //toast(this, items!![0].address)
        if (firestoreAdapter!!.itemCount == 0) {
            Toast.makeText(baseContext, "No Contacts Added", Toast.LENGTH_SHORT).show()
        }
        //firestoreAdapter!!.setOnItemClickListener { view: View, studentItem: StudentItem, i: Int -> }

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
                //chooseContact()
                /*contactsBaseAdapter.notifyDataSetChanged();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);*/return true
            }
            1 ->                 //refresh();
                return true
        }
        return super.onOptionsItemSelected(item)
    }


    /*private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item = firestoreAdapter!!.data[position]
                firestoreAdapter!!.removeItem(position)
                val snackbar = Snackbar.make(
                    coordinatorLayout!!, "Contact removed successfully.", Snackbar.LENGTH_LONG
                )
                snackbar.setAction("UNDO") {
                    firestoreAdapter!!.restoreItem(item, position)
                    recyclerView!!.scrollToPosition(position)
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }*/
}