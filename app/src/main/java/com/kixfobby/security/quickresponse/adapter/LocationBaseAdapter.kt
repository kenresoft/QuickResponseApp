package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.LocationBase
import com.kixfobby.security.quickresponse.storage.LocationBaseManager.removeLocation
import com.kixfobby.security.quickresponse.storage.LocationBaseManager.saveLocation
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class LocationBaseAdapter(private val ctx: Context, val data: ArrayList<LocationBase>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.locationbase_item, parent, false)
        vh = OriginalViewHolder(v)
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is OriginalViewHolder) {
            val view = holder
            val lb = data[position]
            view.location.text = lb.location
            view.time.text = lb.time
            view.lytParent.setOnClickListener { view ->
                if (mOnItemClickListener != null) {
                    mOnItemClickListener!!.onItemClick(view, lb, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun removeItem(position: Int) {
        removeLocation(ctx, data[position])
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    fun restoreItem(item: LocationBase, position: Int) {
        data.add(position, item)
        saveLocation(ctx, item)
        notifyItemInserted(position)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: LocationBase?, position: Int)
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var lytParent: View = v.findViewById(R.id.lyt_parent)
        var location: TextView = v.findViewById(R.id.location_item_location)
        var time: TextView = v.findViewById(R.id.location_item_time)
        var btnRemove: ImageButton? = null

        init {
            //btnRemove = v.findViewById(R.id.location_item_remove);
        }
    }

    private inner class EventDetailSortByDate : Comparator<LocationBase> {
        override fun compare(customerEvents1: LocationBase, customerEvents2: LocationBase): Int {
            val d1 = customerEvents1.time
            val d2 = customerEvents2.time
            val d = DateFormat.getTimeInstance().format(Date())
            return 1
        }
    }

    companion object {
        @JvmStatic
        fun stringToDate(theDateString: String): Date {
            var returnDate = Date()
            if (theDateString.contains("-")) {
                val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm")
                try {
                    returnDate = dateFormat.parse(theDateString)
                } catch (e: ParseException) {
                    val altdateFormat = SimpleDateFormat("dd-MM-yyyy")
                    try {
                        returnDate = altdateFormat.parse(theDateString)
                    } catch (ex: ParseException) {
                        ex.printStackTrace()
                    }
                }
            } else {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
                try {
                    returnDate = dateFormat.parse(theDateString)
                } catch (e: ParseException) {
                    val altdateFormat = SimpleDateFormat("dd/MM/yyyy")
                    try {
                        returnDate = altdateFormat.parse(theDateString)
                    } catch (ex: ParseException) {
                        ex.printStackTrace()
                    }
                }
            }
            return returnDate
        }
    }
}