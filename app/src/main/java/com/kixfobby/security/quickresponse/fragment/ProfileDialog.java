package com.kixfobby.security.quickresponse.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.kixfobby.security.quickresponse.R;

public class ProfileDialog extends DialogFragment {

    TextView iName, iEmail, iPhone, iDescription;

    public static ProfileDialog newInstance(String aNameID, String aPhoneID, String aMessageID) {
        return newInstance(aNameID, aPhoneID, aMessageID, true);
    }

    public static ProfileDialog newInstance(String aNameID, String aPhoneID, String aMessageID, boolean aDoIt) {
        ProfileDialog frag = new ProfileDialog();
        Bundle args = new Bundle();
        args.putString("nameID", aNameID);
        args.putString("phoneID", aPhoneID);
        args.putString("messageID", aMessageID);
        args.putBoolean("keyBoolDoSomething", aDoIt);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile, container, false);

        String mNameID = getArguments().getString("nameID");
        String mEmailID = getArguments().getString("emailID");
        String mPhoneID = getArguments().getString("phoneID");
        String mMessageID = getArguments().getString("messageID");
        final boolean mDoIt = getArguments().getBoolean("keyBoolDoSomething", true);

        getDialog().setTitle("Simple Dialog");

        iName = rootView.findViewById(R.id.item_name);
        iEmail = rootView.findViewById(R.id.item_email);
        iPhone = rootView.findViewById(R.id.item_phone);
        iDescription = rootView.findViewById(R.id.item_description);

        iName.setText(mNameID);
        iEmail.setText(mEmailID);
        iPhone.setText(mPhoneID);
        iDescription.setText(mMessageID);

        rootView.findViewById(R.id.item_button_1_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Okay", Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.CustomDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //dialog.setContentView(R.layout.user_profile);
        dialog.create();
        return dialog;
    }
}
