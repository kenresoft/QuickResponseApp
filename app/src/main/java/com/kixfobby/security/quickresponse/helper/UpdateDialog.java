package com.kixfobby.security.quickresponse.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.kixfobby.security.quickresponse.R;


public class UpdateDialog extends android.app.Dialog {
    android.widget.ImageView ivClose;
    String title;
    String description;
    String version;
    private TextView tvTitle, tvDescription, tvVersion, tvUpdate;
    private Context context;
    private boolean isCancelable;


    public UpdateDialog(Context context, String title, String description, String version, boolean isCancelable) {
        super(context, R.style.CustomDialogTheme);
        // TODO Day selector
        this.context = context;
        this.title = title;
        this.description = description;
        this.version = version;
        this.isCancelable = isCancelable;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.view_app_update);

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvMessage);
        tvVersion = findViewById(R.id.tvVersion);
        tvUpdate = findViewById(R.id.tvUpdateNow);

        ivClose = findViewById(R.id.ivClose);

        if (isCancelable)
            ivClose.setVisibility(View.VISIBLE);
        else
            ivClose.setVisibility(View.GONE);

        ivClose.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        if (!TextUtils.isEmpty(title))
            tvTitle.setText(title);
        else
            tvTitle.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(version))
            tvVersion.setText("Latest version: " + version);
        else
            tvTitle.setVisibility(View.GONE);


        if (!TextUtils.isEmpty(description))
            tvDescription.setText(String.format(description));
        else
            tvDescription.setVisibility(View.GONE);

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // getPackageName() from Context or Activity object
                final String appPackageName = context.getPackageName();

                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException activityNotFoundException) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

    }

}
