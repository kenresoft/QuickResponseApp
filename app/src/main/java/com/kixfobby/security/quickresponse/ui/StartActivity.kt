package com.kixfobby.security.quickresponse.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.ui.ChatRoomActivity
import com.kixfobby.security.quickresponse.util.FBaseUtil

//import com.google.firebase.auth.ExportedUserRecord;
//import com.google.firebase.auth.ListUsersPage;
class StartActivity : BaseActivity() {
    @BindView(R.id.button_enter_chat)
    var buttonEnterChat: Button? = null

    @BindView(R.id.inputNick)
    var editTextNick: EditText? = null

    @BindView(R.id.profilePicture)
    var profilePicture: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        ButterKnife.bind(this)
        //checkUser();
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser!!.uid
        val name = auth.currentUser!!.displayName
        val email = auth.currentUser!!.email

        /*ListUsersPage page = auth.listUsers(null);
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                showToast(user.getUid() + ", ", Toast.LENGTH_LONG);
                //System.out.println("User: " + user.getUid());
            }
            page = page.getNextPage();
        }*/

        /*try {
            User u = retrieveUsers(getApplicationContext()).get(1);
            Toast.makeText(StartActivity.this, u.getPhone() + " " + u.getName() + " " + u.getEmail(), Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getApplicationContext(), "No User Available", Toast.LENGTH_SHORT).show();
        }*/buttonEnterChat!!.setOnClickListener { /*if (isEmpty(editTextNick)) {
                    showToast("Please fill a name!", Toast.LENGTH_LONG);
                    return;
                }*/
            //getStringFromEditText(editTextNick)
            val user = User().setName(name).setEmail(email).setProfilePicture(
                profilePicture!!
            ).setUid(uid)
            val intent = Intent(this@StartActivity, ChatRoomActivity::class.java)
            intent.putExtra("thisUser", user)
            intent.putExtra("thisFriend", "StartActivy")
            FBaseUtil.saveUser(user, this@StartActivity)
            startActivity(intent)
        }
        profilePicture!!.setOnClickListener { callGallery() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_PICTURE && resultCode == RESULT_OK) {
            val uriPicture = data!!.data
            Glide.with(this).load(uriPicture).into(profilePicture!!)
        }
    } /*private void checkUser() {

        User user = FBaseUtil.getSavedUser(StartActivity.this);

        if (user == null) {
            return;
        }

        android.content.Intent intent = new android.content.Intent(StartActivity.this, ChatRoomActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }*/
}