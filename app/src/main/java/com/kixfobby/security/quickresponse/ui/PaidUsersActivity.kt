package com.kixfobby.security.quickresponse.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.PaidUsersHolder
import com.kixfobby.security.quickresponse.model.PaidUsers
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.store.StoreActivity

class PaidUsersActivity : BaseActivity() {
    private var parent_view: View? = null
    private var recyclerView: RecyclerView? = null
    private var paidUsersAdapter: PaidUsersHolder? = null
    private var mFirebaseAdapter: FirebaseRecyclerAdapter<*, *>? = null
    private var ref: DatabaseReference? = null
    private var ref2: DatabaseReference? = null
    private var ref3: DatabaseReference? = null
    private var childEventListener: ChildEventListener? = null
    private var valueEventListener: ValueEventListener? = null
    private var previousSnapshot: DataSnapshot? = null
    private var mAuth: FirebaseAuth? = null
    private var user1: FirebaseUser? = null
    private var isPaymentMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locationbase)
        parent_view = findViewById(R.id.content)

        isPaymentMade = Pref(this).get("isPaymentMade", defValue = false)
        showPaymentDialog()

        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        ref = FirebaseDatabase.getInstance().getReference("Purchase").child(user1!!.uid)
        initComponent()
        supportActionBar?.title = "Paid Users"
    }

    private fun showPaymentDialog() {
        if (isPaymentMade) {
            MaterialAlertDialogBuilder(this)
                .setTitle("User Information for Payment")
                .setMessage(
                    "Save the data of the user to whom the payment will be associated at the end of the payment procedure. \n" +
                            "Please enter the user's correct information. \n" +
                            "Details can't be changed or removed once they've been saved."
                )
                .setView(R.layout.dialog_set_payment_details)
                .setCancelable(false)
                .setBackground(AppCompatResources.getDrawable(this, R.drawable.background_dialog))
                .setIcon(R.drawable.baseline_payment_24)
                .setPositiveButton(getString(R.string.okay)) { mInterface, which ->
                    val email =
                        ((mInterface as? Dialog)!!.findViewById<View>(R.id.edt_email) as TextInputEditText).text.toString()
                    val phone =
                        ((mInterface as? Dialog)!!.findViewById<View>(R.id.edt_phone) as TextInputEditText).text.toString()
                    val mLayoutEmail =
                        ((mInterface as? Dialog)!!.findViewById<View>(R.id.layout_email) as TextInputLayout)
                    val mLayoutPhone =
                        ((mInterface as? Dialog)!!.findViewById<View>(R.id.layout_phone) as TextInputLayout)

                    when {
                        TextUtils.isEmpty(email) -> {
                            toast(this, "Please provide accurate information.")
                        }
                        TextUtils.isEmpty(phone) -> {
                            toast(this, "Please provide accurate information.")
                        }
                        email.length <= 5 -> {
                            toast(this, "Please, enter valid details.")
                        }
                        phone.length <= 5 -> {
                            toast(this, "Please, enter valid details.")
                        }
                        else -> {

                            FirebaseDatabase.getInstance().getReference("Purchase")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid).child(BaseActivity().date)
                                .setValue(PaidUsers(email, phone, BaseActivity().date))

                            FirebaseDatabase.getInstance().getReference("Purchase").child("ALL")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid + " - " + BaseActivity().date)
                                .setValue(email)

                            mInterface.dismiss()
                            Pref(this).put("isPaymentMade", false)
                        }
                    }
                }
                .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAdapter?.startListening()
    }

    private fun initComponent() {
        val query = ref?.orderByChild("date")
        val options: FirebaseRecyclerOptions<PaidUsers> = FirebaseRecyclerOptions.Builder<PaidUsers>()
            .setQuery(query!!, PaidUsers::class.java)
            .build()
        mFirebaseAdapter = object : FirebaseRecyclerAdapter<PaidUsers, PaidUsersHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaidUsersHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_paid_user, parent, false)
                return PaidUsersHolder(view)
            }

            override fun onBindViewHolder(holder: PaidUsersHolder, position: Int, model: PaidUsers) {
                holder.bindMessage(model)
            }
        }

        recyclerView = findViewById<View>(R.id.location_list) as RecyclerView?
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        //recyclerView!!.addItemDecoration(LinearItemDecoration(this, LinearLayout.VERTICAL))
        recyclerView!!.setHasFixedSize(true)

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: ArrayList<PaidUsers?> = ArrayList()
                val td: HashMap<String, PaidUsers> = HashMap()

                for (ds in dataSnapshot.children) {
                    val a: PaidUsers? = ds.getValue(PaidUsers::class.java)
                    td[ds.key!!] = a!!
                    list.add(a)
                }

                var values: ArrayList<PaidUsers> = ArrayList(td.values)
                val keys: List<String> = ArrayList(td.keys)

                for (i in keys.indices) {

                    //Toast.makeText(baseContext, list[i]?.email, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException() // never ignore errors
            }
        }
        ref?.addValueEventListener(valueEventListener as ValueEventListener)

        recyclerView?.adapter = mFirebaseAdapter
        mFirebaseAdapter?.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mFirebaseAdapter?.stopListening()
        childEventListener?.let { ref?.removeEventListener(it) }
        valueEventListener?.let { ref?.removeEventListener(it) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "Make Payment").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_payment_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 1, Menu.NONE, "Reload").setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_refresh_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                startActivity(Intent(baseContext, StoreActivity::class.java))
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            1 -> {
                startActivity(Intent(baseContext, PaidUsersActivity::class.java))
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}