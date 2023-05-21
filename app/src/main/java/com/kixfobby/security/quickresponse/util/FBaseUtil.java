package com.kixfobby.security.quickresponse.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.kixfobby.security.quickresponse.BaseActivity;
import com.kixfobby.security.quickresponse.model.User;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FBaseUtil extends BaseActivity {

    private static final String USER_PREF = "user-pref";

    public static byte[] compressBitmapToByteArray(Bitmap resized, Bitmap.CompressFormat format, int imageQuality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Bitmap bitmap = Bitmap.createScaledBitmap(resized, 300, 300, false);
        bitmap.compress(format, imageQuality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Bitmap getBitmapFromAImageView(ImageView imageView) {
        return ((BitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
    }

    public static String encodeByteToStringBase64(byte[] b) {
        byte[] bytes = Base64.encode(b, Base64.NO_WRAP);
        return new String(bytes);
    }

    public static byte[] decodeStringBase64(String stringBase64) {
        return stringBase64 == null ? null : Base64.decode(stringBase64, Base64.NO_WRAP);
    }

    public static void saveUser(User user, Context context) {
        if (getSavedUser(context) == null) {
            SharedPreferences preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
            preferences.edit().putString("thisUser", new Gson().toJson(user)).apply();
        }
    }

    public static User getSavedUser(android.content.Context context) {
        SharedPreferences preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
        String jsonUserString = preferences.getString("thisUser", null);
        return new Gson().fromJson(jsonUserString, User.class);
    }

    public static void clearSavedUser(Context context) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(USER_PREF, Context.MODE_PRIVATE).edit();
        preferences.clear().apply();
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
    }
}
