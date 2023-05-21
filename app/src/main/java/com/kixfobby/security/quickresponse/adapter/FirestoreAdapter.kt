package com.kixfobby.security.quickresponse.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kixfobby.security.quickresponse.R

class FirestoreAdapter(
    private val notesList: ArrayList<String>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh: RecyclerView.ViewHolder
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        vh = FirestoreAdapter.OriginalViewHolder(v)
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FirestoreAdapter.OriginalViewHolder) {
            val view = holder
            val studentItem = notesList[position]
            /*view.name.text = studentItem.name
            view.age.text = studentItem.age.toString()*/
            //view.address.setOnClickListener { updateNote(studentItem) }
            view.mobile_no.setOnClickListener {
                /*deleteNote(
                    studentItem.name,
                    position
                )*/
            }
        }
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    class OriginalViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var name: TextView
        var age: TextView
        var address: TextView
        var mobile_no: TextView

        init {
            name = v.findViewById(R.id.contact_item_name)
            age = v.findViewById(R.id.contact_item_number)
            address = v.findViewById(R.id.item_registered)
            mobile_no = v.findViewById(R.id.item_active)
        }
    }

    /*private fun updateNote(studentItem: StudentItem) {
        val intent = Intent(
            context,
            StudentItem::class.java
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("UpdateNoteId", studentItem.name)
        intent.putExtra("UpdateNoteTitle", studentItem.age)
        intent.putExtra("UpdateNoteContent", studentItem.address)
        context.startActivity(intent)
    }*/

    /*private fun deleteNote(id: String, position: Int) {
        val firestoreDB: FirebaseFirestore
        firestoreDB.collection("student_info").document("student_list").delete()
            .addOnCompleteListener {
                notesList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, notesList.size)
                Toast.makeText(context, "Note has been deleted!", Toast.LENGTH_SHORT).show()
            }
    }*/
}

