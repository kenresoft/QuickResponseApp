//package com.kixfobby.security.quickresponse.ui;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.kixfobby.security.quickresponse.fragment.SetFrag;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.Objects;
//
//public class Settings extends BaseActivity {
//    private SetFrag fragment;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
//
//        fragment = new SetFrag();
//        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onBackPressed() {
//        Intent r = new Intent(getApplicationContext(), Form.class);
//        r.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(r);
//        finish();
//
//        super.onBackPressed();
//    }
//}
