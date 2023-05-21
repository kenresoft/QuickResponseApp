package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.LocationBaseAdapter
import com.kixfobby.security.quickresponse.helper.SwipeToDeleteCallback
import com.kixfobby.security.quickresponse.model.LocationBase
import com.kixfobby.security.quickresponse.storage.*

class LocationBaseActivity : BaseActivity() {
    private var listLocationBase: LocationBaseAdapter? = null
    private var coordinatorLayout: CoordinatorLayout? = null
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locationbase)
        supportActionBar!!.setTitle(R.string.manage_location)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        listLocationBase = LocationBaseAdapter(this, LocationBaseManager.getSavedLocation(applicationContext))
        recyclerView = findViewById(R.id.location_list)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.isNestedScrollingEnabled = false
        recyclerView!!.adapter = listLocationBase
        listLocationBase!!.notifyDataSetChanged()
        enableSwipeToDeleteAndUndo()
        listLocationBase!!.setOnItemClickListener(object : LocationBaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, obj: LocationBase?, position: Int) {
                val latEnd = obj!!.location.indexOf("Longitude:") - 2
                val longStart = obj.location.indexOf("Longitude:") + 10
                val longEnd = obj.location.indexOf(";")
                val latitude = java.lang.Double.valueOf(obj.location.substring(9, latEnd).trim { it <= ' ' })
                val longitude = java.lang.Double.valueOf(obj.location.substring(longStart, longEnd).trim { it <= ' ' })
                val imap = Intent(baseContext, MapsActivity::class.java)
                val bundle = Bundle()
                bundle.putDouble("long", longitude)
                bundle.putDouble("lat", latitude)
                imap.putExtras(bundle)
                //overridePendingTransition(R.anim.shrink_fade_out_center, R.anim.shrink_fade_out_center)
                startActivity(imap)
            }
        })
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeToDeleteCallback: SwipeToDeleteCallback = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                val position = viewHolder.adapterPosition
                val item = listLocationBase!!.data[position]
                listLocationBase!!.removeItem(position)
                val snackbar = Snackbar.make(
                    coordinatorLayout!!, "Location data removed successfully.", Snackbar.LENGTH_LONG
                )
                snackbar.setAction("UNDO") {
                    listLocationBase!!.restoreItem(item, position)
                    recyclerView!!.scrollToPosition(position)
                }
                snackbar.setActionTextColor(Color.YELLOW)
                snackbar.show()
            }
        }
        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchhelper.attachToRecyclerView(recyclerView)
    }
}