package com.kixfobby.security.quickresponse.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class CustomLinearManager extends LinearLayoutManager {

    public CustomLinearManager(Context context) {
        super(context);
    }

    public CustomLinearManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

// Something is happening here

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}

