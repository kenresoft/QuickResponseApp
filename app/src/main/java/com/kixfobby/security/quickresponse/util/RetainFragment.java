package com.kixfobby.security.quickresponse.util;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class RetainFragment extends Fragment {

    private static final String TAG = "com.kixfobby.security.quickresponse.retainfragment";
    private Object mObject;

    public RetainFragment() {
    }

    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        final RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
        if (fragment != null) {
            return fragment;
        }
        return new RetainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public Object getObject() {
        return mObject;
    }

    public void setObject(Object obj) {
        mObject = obj;
    }

}
