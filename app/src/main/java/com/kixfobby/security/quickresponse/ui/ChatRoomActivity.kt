package com.kixfobby.security.quickresponse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.kixfobby.security.floaty.UI.FloatingActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.adapter.ChatViewHolder
import com.kixfobby.security.quickresponse.databinding.ActivityChatRoomBinding
import com.kixfobby.security.quickresponse.model.Message
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.storage.Pref

class ChatRoomActivity : FloatingActivity() {
    /* @JvmField
     @BindView(R.id.chat_list)*/
    var recyclerView: RecyclerView? = null

    //@BindView(R.id.button_send_message)
    //FloatingActionButton sendButton;
    //@BindView(R.id.button_send_not_able)
    //FloatingActionButton sendButtonNotAble;
    //@BindView(R.id.message_chat)
    //EditText inputMessage;
    /* @JvmField
     @BindView(R.id.chat_list_progress)*/
    var progressBar: ProgressBar? = null

    /*@JvmField
    @BindView(R.id.progress_text)*/
    var progressText: TextView? = null

    /*@JvmField
    @BindView(R.id.no_messages)*/
    var noMessageText: TextView? = null

    /* @JvmField
     @BindView(R.id.sad_icon)*/
    var sadIcon: ImageView? = null

    private var linearLayoutManager: LinearLayoutManager? = null
    private var chatReference: DatabaseReference? = null
    private var userReference: DatabaseReference? = null
    private var me: DatabaseReference? = null
    private var you: DatabaseReference? = null
    private var myFriends: DatabaseReference? = null
    private var yourFriends: DatabaseReference? = null
    private val myStatus: DatabaseReference? = null
    private var mFriend: DatabaseReference? = null
    private var yFriend: DatabaseReference? = null
    private val myMessageDate: DatabaseReference? = null
    private val yourMessageDate: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private var user1: FirebaseUser? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<*, *>? = null
    private var user: User? = null
    private var childEventListener2: ChildEventListener? = null
    private val childEventListener1: ValueEventListener? = null
    private var fri: String? = null
    private var s: String? = null
    private lateinit var binding: ActivityChatRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        recyclerView = binding.chatList
        //progressBar = binding.chatListProgress
        //progressText = binding.progressText
        noMessageText = binding.noMessages
        //sadIcon = binding.sadIcon

        //ButterKnife.bind(this)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.setHasFixedSize(true)
        recyclerView?.setItemViewCacheSize(10000)
        recyclerView?.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        user = intent.extras?.getSerializable("thisUser") as User?
        fri = intent.getStringExtra("thisFriend")

        //showNotifAlert();
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth?.currentUser
        s = if (fri != null) fri else user1?.uid
        userReference = FirebaseDatabase.getInstance().getReference("User")
        chatReference = FirebaseDatabase.getInstance().getReference("Chat")
        me = user1?.uid?.let { chatReference?.child(it) }
        you = chatReference?.child(s!!)
        myFriends = me?.child("Friends")
        yourFriends = you?.child("Friends")

        //myStatus = me.child("Status");
        mFriend = myFriends?.child(s!!)
        yFriend = user1?.uid?.let { yourFriends?.child(it) }

        Pref(this).put("mFriend", mFriend.toString())
        //Toast.makeText(baseContext, mFriend.toString(), Toast.LENGTH_LONG).show()


        /*TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    sendButton.setVisibility(android.view.View.VISIBLE);
                    sendButtonNotAble.setVisibility(android.view.View.INVISIBLE);
                }

                */
        /*Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
                Matcher ms = ps.matcher(inputMessage.getText().toString());
                boolean bs = ms.matches();
                if (!bs) {
                    Toast.makeText(getBaseContext(), "Retry!", Toast.LENGTH_LONG).show();
                }*/
        /*
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (s.toString().length() == 0) {
                    sendButton.setVisibility(android.view.View.INVISIBLE);
                    sendButtonNotAble.setVisibility(android.view.View.VISIBLE);
                    com.daimajia.androidanimations.library.YoYo.with(com.daimajia.androidanimations.library.Techniques.RubberBand).duration(400).playOn(sendButtonNotAble);
                }

                */
        /*String currentText = s.toString();
                int currentLength = currentText.length();
                Toast.makeText(getBaseContext(), String.valueOf(currentLength), Toast.LENGTH_LONG).show();*/
        /*
            }
        };

        inputMessage.addTextChangedListener(textWatcher);*/

        val query = mFriend?.limitToLast(50)
        val options: FirebaseRecyclerOptions<Message> = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(query!!, Message::class.java)
            .build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Message, ChatViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
                val mView: View = LayoutInflater.from(parent.context).inflate(R.layout.chat_item_list, parent, false)
                return ChatViewHolder(mView)
            }

            override fun onBindViewHolder(holder: ChatViewHolder, position: Int, model: Message) {
                disableProgress()
                supportActionBar?.setTitle(model.senderName)
                holder.bindMessage(model)
            }
        }

        /*firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Message, ChatViewHolder>(
            Message::class.java, R.layout.chat_item_list, ChatViewHolder::class.java, mFriend
        ) {

            *//*override fun populateViewHolder(viewHolder: AdapterListExpand, model: AdminMsg?, position: Int) {
                if (model != null) {
                    viewHolder.bindMessage(model)
                }
            }*//*
            override fun populateViewHolder(viewHolder: ChatViewHolder, model: Message, position: Int) {
                disableProgress()
                supportActionBar?.setTitle(model.senderName)
                viewHolder.bindMessage(model)
                //if (model.getReceiverUid().equals(fri)) {
                //viewHolder.othersMessageLayout.setVisibility(android.view.View.INVISIBLE);
                //viewHolder.yourMessage.setText(model.getMessage());
                //viewHolder.yourDate.setText(model.getDate());
                //else {
                *//*if (model.getReceiverUid().equals(user1.getUid())) {*//*
                //viewHolder.yourMessageLayout.setVisibility(android.view.View.INVISIBLE);
                *//*viewHolder.othersMessage?.text = model.message
                viewHolder.otherName?.text = model.senderName
                viewHolder.othersDate?.text = model.date*//*
                //loadProfilePicture(com.kixfobby.security.quickresponse.util.FBaseUtil.decodeStringBase64(model.getUserPicture()), viewHolder.profilePicture);
                *//* }*//*
            }
        }
*/

        /*firebaseRecyclerAdapter = new com.firebase.ui.database.FirebaseRecyclerAdapter<Message, ViewHolderChat>(Message.class, R.layout.chat_item_list, ViewHolderChat.class, yFriend) {
            @Override
            protected void populateViewHolder(ViewHolderChat viewHolder, Message model, int position) {

                disableProgress();

                //if (model.getReceiverUid().equals(fri)) {
                viewHolder.othersMessageLayout.setVisibility(android.view.View.INVISIBLE);
                viewHolder.yourMessage.setText(model.getMessage());
                viewHolder.yourDate.setText(model.getDate());
                // else {
                */
        /*if (model.getReceiverUid().equals(user1.getUid())) {
                //viewHolder.yourMessageLayout.setVisibility(android.view.View.INVISIBLE);
                //viewHolder.othersMessage.setText(model.getMessage());
                //viewHolder.otherName.setText(model.getReceiverName());
                //viewHolder.othersDate.setText(model.getDate());
                //loadProfilePicture(FBaseUtil.decodeStringBase64(model.getUserPicture()), viewHolder.profilePicture);
                }*/
        /*
            }
        };*/

        /*sendButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {

                //String message = inputMessage.getText().toString();

                myMessageDate = mFriend.child(getDate());
                yourMessageDate = yFriend.child(getDate());

                yourMessageDate.setValue(new Message()
                        .setMessage(message)
                        //.setUserPicture(user.getProfilePicture())
                        .setSenderName(user.getName())
                        //.setSenderUid(user.getUid())
                        .setMessageStatus("Not seen")
                        .setMessageType("Inbox")
                        .setDate(getDate()));

                */
        /*myMessageDate.setValue(new Message()
                        .setMessage(message)
                        //.setUserPicture(user.getProfilePicture())
                        //.setReceiverName(user.getName())
                        //.setReceiverUid(fri)
                        .setMessageStatus("Not delivered")
                        .setMessageType("Outbox"));*/
        /*


                inputMessage.setText("");
            }
        });*/

        //MESSAGE INFORMATION LIST
        /*mFriend.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> list = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message value = ds.getValue(Message.class);
                    list.add(value);
                }
                //Log.d("TAG", list.toString());
                //Toast.makeText(getBaseContext(), list.get(0).getReceiverUid().toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/


        //MESSAGE LIST
        childEventListener2 = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                updateChatList()
                disableNoMessages()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                showNoMessages()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                //showToast(databaseError.message, Toast.LENGTH_SHORT)
            }
        }
        mFriend?.addChildEventListener(childEventListener2!!)
        recyclerView?.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter?.notifyDataSetChanged()
        //val handler = Handler()
        /*handler.postDelayed({
            if (linearLayoutManager?.itemCount == 0) {
                showNoMessages()
                disableProgress()
            }
        }, 4000)*/
    }

    private fun showNoMessages() {
        if (linearLayoutManager?.itemCount == 0) {
            //sadIcon?.visibility = View.VISIBLE
            noMessageText?.setVisibility(android.view.View.VISIBLE);
        }
    }

    private fun disableNoMessages() {
        if (noMessageText?.visibility == View.VISIBLE) {
            //sadIcon?.visibility = View.GONE
            noMessageText?.setVisibility(android.view.View.GONE);
        }
    }

    public fun updateChatList() {
        if (firebaseRecyclerAdapter?.itemCount == 0) {
            return
        }
        firebaseRecyclerAdapter?.itemCount?.minus(1)?.let { recyclerView?.smoothScrollToPosition(it) }
    }

    private fun loadProfilePicture(pictureBytes: ByteArray, imageView: ImageView) {
        Glide.with(this).load(pictureBytes)
            .fallback(R.drawable.ic_fallback_user)
            .skipMemoryCache(true)
            .into(imageView)
    }

    override fun onStart() {
        super.onStart()
        mFriend?.addChildEventListener(childEventListener2!!)
        firebaseRecyclerAdapter?.startListening()
    }

    /*override fun onStop() {
        super.onStop()
        mFriend?.removeEventListener(childEventListener2!!)
        firebaseRecyclerAdapter?.stopListening()
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        firebaseRecyclerAdapter?.stopListening()
        //userReference.removeEventListener(childEventListener1);
        mFriend?.removeEventListener(childEventListener2!!)
    }

    /*@Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
            alertDialog.setIcon(R.drawable.ic_fallback_user).setTitle("Do you really want to leave ?")
                    .setMessage("If you leave your user will be eraser, because your user is locally saved.")
                    .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(android.content.DialogInterface dialog, int which) {
                            FBaseUtil.clearSavedUser(ChatRoomActivity.this);
                            finish();
                        }
                    }).setNegativeButton("No", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {

                }
            }).show();
        }

        return true;
    }*/
    private fun disableProgress() {
        if (progressBar?.visibility == View.VISIBLE) {
            progressBar?.visibility = View.GONE
            progressText?.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    companion object {
        var notifyMesasage = "Please wait..."
    }
}