package com.kixfobby.security.floaty.UI;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.kixfobby.security.localization.ui.LocalizationActivity;

public class FloatingActivity extends LocalizationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final View view = getWindow().getDecorView();
        final WindowManager.LayoutParams lp = (WindowManager.LayoutParams) view.getLayoutParams();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //  params.setMargins(5, 20, 5, 20);

        lp.gravity = Gravity.CENTER;

        lp.width = params.width;
        lp.height = params.height;
        // view.setLayoutParams(params);

        getWindowManager().updateViewLayout(view, lp);

    }

}
