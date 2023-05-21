package com.kixfobby.security.quickresponse.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.kixfobby.security.quickresponse.R;
import com.kixfobby.security.quickresponse.adapter.SmsBaseAdapter;
import com.kixfobby.security.quickresponse.model.SmsBase;
import com.kixfobby.security.quickresponse.storage.SmsBaseManager;
import com.kixfobby.security.quickresponse.widget.CustomLinearManager;

public class SmsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.activity_smsbase, container, false);
        final FragmentActivity c = getActivity();
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.sms_list);
        CustomLinearManager layoutManager = new CustomLinearManager(c);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                recyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        SmsBaseAdapter listSmsBase = new SmsBaseAdapter(getActivity(), SmsBaseManager.getSavedSms(getActivity()));
                        recyclerView.setAdapter(listSmsBase);
                        listSmsBase.notifyDataSetChanged();

                        listSmsBase.setOnItemClickListener(new SmsBaseAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, SmsBase obj, int position) {

                                //showDialog(obj.getName(), obj.getNumber(), obj.getMessage(), false);
                            }
                        });

                    }
                });
            }
        }).start();

        return view;
    }


    private void showDialog(String aNameID, String aNumberID, String aMessageID, boolean aDoIt) {
        DialogFragment uiDialogMessage = ProfileDialog.newInstance(aNameID, aNumberID, aMessageID, aDoIt);
        uiDialogMessage.show(getFragmentManager(), "dialog");
    }
}