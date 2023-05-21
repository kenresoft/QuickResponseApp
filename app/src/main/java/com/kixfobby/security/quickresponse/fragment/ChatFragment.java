package com.kixfobby.security.quickresponse.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.kixfobby.security.quickresponse.R;
import com.kixfobby.security.quickresponse.adapter.ChatBaseAdapter;
import com.kixfobby.security.quickresponse.model.ChatBase;
import com.kixfobby.security.quickresponse.model.User;
import com.kixfobby.security.quickresponse.storage.ChatBaseManager;
import com.kixfobby.security.quickresponse.ui.ChatRoomActivity;
import com.kixfobby.security.quickresponse.util.FBaseUtil;
import com.kixfobby.security.quickresponse.widget.CustomLinearManager;

import java.util.List;

public class ChatFragment extends androidx.fragment.app.Fragment {

    private String uid1;

    @Override
    public android.view.View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_smsbase, container, false);
        final FragmentActivity c = getActivity();
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.sms_list);
        CustomLinearManager layoutManager = new CustomLinearManager(c);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        String uid = auth.getCurrentUser().getUid();
        String name = auth.getCurrentUser().getDisplayName();
        String phone = auth.getCurrentUser().getPhoneNumber();

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                ChatBaseAdapter listChatBase = new ChatBaseAdapter(getActivity(), ChatBaseManager.getSavedChat(getActivity()));
                recyclerView.setAdapter(listChatBase);

                listChatBase.notifyDataSetChanged();

                listChatBase.setOnItemClickListener(new ChatBaseAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, ChatBase obj, int position) {

                        List<User> it = new com.kixfobby.security.quickresponse.service.UpdateService().retrieveUsers(getActivity());
                        User user = new User().setName(name).setPhone(phone).setUid(uid);

                        Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
                        intent.putExtra("thisUser", user);
                        //Toast.makeText(c, String.valueOf(it.size()), Toast.LENGTH_SHORT).show();
                        for (User u : it) {
                            if (u.getPhone().trim().equals(obj.getNumber())) {
                                obj.setUid(u.getUid());
                                uid1 = obj.getUid();
                                //Toast.makeText(getActivity(), uid1, Toast.LENGTH_SHORT).show();

                                try {
                                    intent.putExtra("thisFriend", uid1);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    intent.putExtra("thisFriend", "uid1");
                                }
                            }

                        }

                        FBaseUtil.saveUser(user, getActivity());
                        ChatFragment.this.startActivity(intent);

                        //showDialog(obj.getName(), obj.getNumber(), obj.getMessage(), false);
                    }
                });
            }
        });

        return view;
    }


    private void showDialog(String aNameID, String aNumberID, String aMessageID, boolean aDoIt) {
        androidx.fragment.app.DialogFragment uiDialogMessage = ProfileDialog.newInstance(aNameID, aNumberID, aMessageID, aDoIt);
        uiDialogMessage.show(this.getFragmentManager(), "dialog");
    }
}