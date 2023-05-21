package com.kixfobby.security.quickresponse.model

import android.view.View
import butterknife.BindView
import com.kixfobby.security.quickresponse.R
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife

class ViewHolderChat(itemView: View?) : RecyclerView.ViewHolder(
    itemView!!
) {
    /*public @butterknife.BindView(R.id.yourMessageView)
    RelativeLayout yourMessageLayout;
    public @butterknife.BindView(R.id.your_message)
    TextView yourMessage;
    public @butterknife.BindView(R.id.your_date)
    TextView yourDate;*/
    @JvmField
    @BindView(R.id.other_name)
    var otherName: TextView? = null

    @JvmField
    @BindView(R.id.other_date)
    var othersDate: TextView? = null

    @JvmField
    @BindView(R.id.others_message)
    var othersMessage: TextView? = null

    /*public @butterknife.BindView(R.id.otherMessageView)
    RelativeLayout othersMessageLayout;
    public @butterknife.BindView(R.id.otherPicture)
    ImageView profilePicture;*/
    init {
        ButterKnife.bind(this, itemView!!)
    }
}