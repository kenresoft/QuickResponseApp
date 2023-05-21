package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kixfobby.security.badge.ImageBadgeView
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.AdapterListExpand
import com.kixfobby.security.quickresponse.model.AdminMsg


class MessageActivity : BaseActivity() {
    private var parent_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AdapterListExpand? = null
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<*, *>? = null
    private var ref: DatabaseReference? = null
    private var ref2: DatabaseReference? = null
    private var ref3: DatabaseReference? = null
    private var childEventListener: ChildEventListener? = null
    private var valueEventListener: ValueEventListener? = null
    private var previousSnapshot: DataSnapshot? = null
    private var mAuth: FirebaseAuth? = null
    private var user1: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_expand)
        parent_view = findViewById<View>(R.id.content)
        var msg: TextView = findViewById<TextView>(R.id.text)
        var img: ImageBadgeView = findViewById<ImageBadgeView>(R.id.img)

        //showNotifAlert();
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        ref = FirebaseDatabase.getInstance().getReference("Admin").child("Message").child(user1!!.uid)
        ref2 = FirebaseDatabase.getInstance().getReference("Admin").child("Message").child("GENERAL")
        //ref3 = FirebaseDatabase.getInstance().getReference("Admin").child("Message").child(user1!!.uid).child("0")

        msg.text = PreferenceManager.getDefaultSharedPreferences(this).getString("vmessage", "message")
        initComponent()

    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter?.startListening()
    }

    /*override fun onStop() {
        super.onStop()
        mFirebaseAdapter?.stopListening()
    }*/

    private fun initComponent() {
        /*val baseQuery: Query = mDatabase.getReference().child("items")
        val config = PagingConfig(20, 10, false)
        val options2 = DatabasePagingOptions.Builder<Item>()
            .setLifecycleOwner(this)
            .setQuery(baseQuery, config, Item::class.java)
            .build()*/

        val query = ref?.limitToLast(50)
        val options: FirebaseRecyclerOptions<AdminMsg> = FirebaseRecyclerOptions.Builder<AdminMsg>()
            .setQuery(query!!, AdminMsg::class.java)
            .build()
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<AdminMsg, AdapterListExpand>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterListExpand {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_expand, parent, false)
                return AdapterListExpand(view)
            }

            override fun onBindViewHolder(holder: AdapterListExpand, position: Int, model: AdminMsg) {
                holder.bindMessage(model)
            }
        }

        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView?
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //recyclerView!!.addItemDecoration(LinearItemDecoration(this, LinearLayout.VERTICAL))
        recyclerView!!.setHasFixedSize(true)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: ArrayList<AdminMsg?> = ArrayList();
                val td: HashMap<String, AdminMsg> = HashMap()

                for (ds in dataSnapshot.children) {
                    val a: AdminMsg? = ds.getValue(AdminMsg::class.java)
                    td[ds.key!!] = a!!
                    list.add(a)
                }

                var values: ArrayList<AdminMsg> = ArrayList(td.values)
                val keys: List<String> = ArrayList(td.keys)

                for (i in keys.indices) {

                    //Toast.makeText(baseContext, list[i]?.title, Toast.LENGTH_SHORT).show()
                }
                //Toast.makeText(baseContext, keys., Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException() // never ignore errors
            }
        }

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                updateChatList()
                for (i in 0..dataSnapshot.childrenCount.minus(1)) {
                    var seen: String =
                        PreferenceManager.getDefaultSharedPreferences(baseContext).getBoolean("$i seen", false)
                            .toString()
                    //Toast.makeText(baseContext, seen, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                for (child: DataSnapshot in dataSnapshot.children) {
                    val cc: String = child.key.toString()
                    //Toast.makeText(baseContext, s, Toast.LENGTH_SHORT).show()
                    PreferenceManager.getDefaultSharedPreferences(baseContext).edit()
                        .putBoolean("$cc seen", false)
                        .apply()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                for (child: DataSnapshot in dataSnapshot.children) {
                    val cc: String = child.key.toString()
                    //Toast.makeText(baseContext, cc, Toast.LENGTH_SHORT).show()
                    PreferenceManager.getDefaultSharedPreferences(baseContext).edit()
                        .putBoolean("$cc seen", false)
                        .apply()
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                for (child: DataSnapshot in dataSnapshot.children) {
                    val cc: String = child.key.toString()
                    //Toast.makeText(baseContext, cc, Toast.LENGTH_SHORT).show()
                    PreferenceManager.getDefaultSharedPreferences(baseContext).edit()
                        .putBoolean("$cc seen", false)
                        .apply()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }
        ref2?.addChildEventListener(childEventListener as ChildEventListener)
        ref?.addValueEventListener(valueEventListener as ValueEventListener)

        recyclerView?.adapter = mFirebaseAdapter
        mFirebaseAdapter?.notifyDataSetChanged()
    }

    internal fun updateChatList() {
        if (mFirebaseAdapter?.itemCount == 0) {
        }
        mFirebaseAdapter?.itemCount.takeIf { it!! > 0 }?.let { recyclerView?.smoothScrollToPosition(it) }
        /*(mFirebaseAdapter?.itemCount)?.minus(1)?.let{}*/
    }

    override fun onDestroy() {
        super.onDestroy()
        mFirebaseAdapter?.stopListening()
        childEventListener?.let { ref?.removeEventListener(it) }
        childEventListener?.let { ref2?.removeEventListener(it) }
        valueEventListener?.let { ref?.removeEventListener(it) }
    }

}