package com.kixfobby.security.quickresponse.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.kixfobby.security.permission.PermissionHandler;
import com.kixfobby.security.permission.Permissions;
import com.kixfobby.security.quickresponse.BaseActivity;
import com.kixfobby.security.quickresponse.R;
import com.kixfobby.security.quickresponse.databinding.ActivitySelfBinding;
import com.kixfobby.security.quickresponse.model.ChatBase;
import com.kixfobby.security.quickresponse.model.ContactBase;
import com.kixfobby.security.quickresponse.model.LocationBase;
import com.kixfobby.security.quickresponse.model.Message;
import com.kixfobby.security.quickresponse.model.SmsBase;
import com.kixfobby.security.quickresponse.model.User;
import com.kixfobby.security.quickresponse.storage.ChatBaseManager;
import com.kixfobby.security.quickresponse.storage.Constants;
import com.kixfobby.security.quickresponse.storage.ContactManager;
import com.kixfobby.security.quickresponse.storage.LocationBaseManager;
import com.kixfobby.security.quickresponse.storage.PageDatabase;
import com.kixfobby.security.quickresponse.storage.SmsBaseManager;
import com.kixfobby.security.quickresponse.viewholder.ViewHolderChat;
import com.kixfobby.security.quickresponse.viewmodel.BillingVM;
import com.kixfobby.security.quickresponse.viewmodel.SelfVM;
import com.kixfobby.security.quickresponse.widget.BillingPremiumDialog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.kixfobby.security.quickresponse.storage.Constants.CURRENT_LOCATION;
import static com.kixfobby.security.quickresponse.storage.Constants.CUSTOM_LOCATION;
import static com.kixfobby.security.quickresponse.storage.Constants.SMS_MESSAGE;

public class Self extends BaseActivity {
    private static final String TAG = Self.class.getSimpleName();
    private static final int REQUEST_CODE = 1;
    // location updates interval - 5sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    // fastest updates interval - 1 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final String LOG_TAG = "CheckNetworkStatus";
    public static String ad1;
    public static String ad2;
    public static String ad3;
    private Activity context;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Timer timer;
    private TimerTask timerTask;
    private PageDatabase db;
    private Cursor c;
    private boolean row;
    private String id, page;
    private Button btnPanic;
    private TextView textView, textMap;
    private CoordinatorLayout coordinatorLayout;
    private Animation animation;
    private NetworkChangeReceiver receiver;
    private IntentFilter filter;
    private boolean isConnected = false;
    private Address locationAddress;
    private String mLocation;
    private String mAddress;
    private String mTime;
    private String mLastUpdateTime;
    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private ActivitySelfBinding selfBinding;
    private DatabaseReference chatReference, userReference;
    private DatabaseReference me, you, myFriends, yourFriends, myStatus, mFriend, yFriend, myMessageDate, yourMessageDate;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user1;
    private FirebaseRecyclerAdapter<Message, ViewHolderChat> firebaseRecyclerAdapter;
    private User user;
    private ChildEventListener childEventListener2;
    private ValueEventListener childEventListener1;
    private String fri;
    private BottomNavigationView bottomNavigationView;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(getBaseContext(), Self.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                    return true;
                case R.id.navigation_dashboard:
                    startActivity(new Intent(getBaseContext(), Dashboard.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                case R.id.navigation_account:
                    startActivity(new Intent(getBaseContext(), ManageAccount.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
            }
            return false;
        }
    };

    private Observer<Boolean> isPremiumPurchasedObserver = new Observer<Boolean>() {
        @Override
        public void onChanged(@Nullable Boolean aBoolean) {
            if (aBoolean != null) {
                // Dismisses BillingPremiumDialog after successful purchase of Premium Feature.

                if (aBoolean || user1.getEmail().equalsIgnoreCase(ad1) || user1.getEmail().equalsIgnoreCase(ad2) || user1.getEmail().equalsIgnoreCase(ad3)) {
                    BillingPremiumDialog.dismiss(Self.this);

                    btnPanic.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {

                            btnPanic.startAnimation(animation);

                            if ((ContextCompat.checkSelfPermission(Self.this, Manifest.permission.SEND_SMS) +
                                    ContextCompat.checkSelfPermission(Self.this, Manifest.permission.SEND_SMS))
                                    != PackageManager.PERMISSION_GRANTED) {

                                if (ActivityCompat.shouldShowRequestPermissionRationale(Self.this, "Manifest.permission.SEND_SMS") ||
                                        ActivityCompat.shouldShowRequestPermissionRationale(Self.this, "Manifest.permission.SEND_SMS")) {
                                    // Show an explanation to the user *asynchronously* -- don't block
                                    // this thread waiting for the user's response! After the user
                                    // sees the explanation, try again to request the permission.
                                    Toast.makeText(Self.this, "Please provide app permission so that you can send sms", Toast.LENGTH_SHORT).show();
                                } else {
                                    // No explanation needed; request the permission
                                    ActivityCompat.requestPermissions(Self.this,
                                            new String[]{"Manifest.permission.SEND_SMS"}, REQUEST_CODE);
                                }
                            } else {
                                // Permission has already been granted
                                String messageText = PreferenceManager.getDefaultSharedPreferences(Self.this).getString(SMS_MESSAGE, null);
                                boolean currentLocation = PreferenceManager.getDefaultSharedPreferences(Self.this).getBoolean(CURRENT_LOCATION, false);
                                String customLocation = PreferenceManager.getDefaultSharedPreferences(Self.this).getString(CUSTOM_LOCATION, null);
                                String country = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("country", "country");
                                String state = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("state", "state");
                                String zip = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("zip", "zip");

                                if (messageText == null) {
                                    messageText = "I need help come ASAP!";
                                }
                                if (!currentLocation) {
                                    messageText += "\n" + mLocation;
                                } else if (customLocation != null) {
                                    messageText += "\n" + customLocation;
                                } else
                                    messageText += "\n" + zip + ", " + state + ", " + country;

                                SmsManager smsMan = SmsManager.getDefault();
                                ArrayList<ContactBase> recipients = ContactManager.getSavedPersons(getApplicationContext());
                                if(recipients.size() != 0){
                                    for (ContactBase p : recipients) {
                                        smsMan.sendTextMessage(p.getNumber(), null, messageText, null, null);

                                        SmsBase s = new SmsBase(p.getNumber(), p.getName(), messageText, getDate());
                                        SmsBaseManager.saveSms(s, getBaseContext());
                                    }

                                    Set uid = PreferenceManager.getDefaultSharedPreferences(Self.this).getStringSet("contacts_uid_set", java.util.Collections.singleton("Contact Uid"));
                                    List<String> uidList = new ArrayList<String>();
                                    uidList.addAll(uid);

                                    Set name = PreferenceManager.getDefaultSharedPreferences(Self.this).getStringSet("contacts_name_set", java.util.Collections.singleton("Contact Name"));
                                    List<String> nameList = new ArrayList<String>();
                                    nameList.addAll(name);

                                    Set phone = PreferenceManager.getDefaultSharedPreferences(Self.this).getStringSet("contacts_phone_set", java.util.Collections.singleton("Contact Phone"));
                                    List<String> phoneList = new ArrayList<String>();
                                    phoneList.addAll(phone);

                                    for (int i = 0; i < uidList.size(); i++) {
                                        mFriend = myFriends.child(uidList.get(i).toString());

                                        you = chatReference.child(uidList.get(i).toString());
                                        yourFriends = you.child("Friends");
                                        yFriend = yourFriends.child(user1.getUid());

                                        myMessageDate = mFriend.child(getDate());
                                        yourMessageDate = yFriend.child(getDate());

                                        yourMessageDate.setValue(new Message()
                                                .setMessage(messageText)
                                                //.setUserPicture(user.getProfilePicture())
                                                .setSenderName(nameList.get(i).toString())
                                                //.setSenderUid(user.getUid())
                                                .setMessageStatus("Not seen")
                                                .setMessageType("Inbox")
                                                .setDate(getDate()));

                                        ChatBase s = new ChatBase(phoneList.get(i).toString(), nameList.get(i).toString(), messageText, getDate());
                                        ChatBaseManager.saveChat(s, getBaseContext());
                                    }

                                    showAlert(messageText);

                                }else{
                                    Snackbar.make(coordinatorLayout, "No Security contact saved!", Snackbar.LENGTH_LONG).show();
                                }

                            }

                            return false;
                        }
                    });
                }

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selfBinding = DataBindingUtil.setContentView(this, R.layout.activity_self);
        coordinatorLayout = findViewById(R.id.coordinator);
        animation = AnimationUtils.loadAnimation(this, R.anim.shake);
        bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        init();
        mainUi();
        initComponent();
        initLocation();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.self_alert);
        restoreValuesFromBundle(savedInstanceState);
        mRequestingLocationUpdates = true;
        updateLocationUI();
        btnPanic = findViewById(R.id.btn_panic);
        btnPanic.setAnimation(AnimationUtils.loadAnimation(Self.this, R.anim.blink));

        ad1 = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("vadmin1", "adm1");
        ad2 = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("vadmin2", "adm2");
        ad3 = PreferenceManager.getDefaultSharedPreferences(Self.this).getString("vadmin3", "adm3");

    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPanic.setAlpha(0);
        btnPanic.animate().alpha(1).setDuration(200);
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }
        bottomNavigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RestartServiceBroadcastReceiver.scheduleJob(getApplicationContext());
        } else {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(getApplicationContext());
        }*/
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);
        updateLocationUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RestartServiceBroadcastReceiver.scheduleJob(getApplicationContext());
        } else {
            ProcessMainClass bck = new ProcessMainClass();
            bck.launchService(getApplicationContext());
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*Intent broadcastIntent = new Intent(Constants.RESTART_INTENT);
        sendBroadcast(broadcastIntent);*/
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void initComponent() {
        db = new PageDatabase(this);
        c = db.getAllPages();
        row = c.moveToLast();
        if (row) {
            id = c.getString(0);
            page = c.getString(1);
        }
        db.updatePage(Constants.SELF);

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE};
        String rationale = "Please provide app permission so that you can use all app features";
        Permissions.Options options = new Permissions.Options()
                .setRationaleDialogTitle("Info")
                .setSettingsDialogTitle("Warning");

        Permissions.check(this, permissions, rationale, options, new PermissionHandler() {
            @Override
            public void onGranted() {
                //Toast.makeText(Self.this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                Toast.makeText(Self.this, "Permission denied.", Toast.LENGTH_SHORT).show();
                openSettings();
            }
        });
    }

    private void mainUi() {
        textView = findViewById(R.id.tvStatus);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
            }
        });

        textMap = findViewById(R.id.tvMap);
        textMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

    }

    private void init() {
        SelfVM selfVM = new ViewModelProvider(this).get(SelfVM.class);
        BillingVM billingVM = new ViewModelProvider(this).get(BillingVM.class);
        selfBinding.setPresenter(selfVM);
        this.getLifecycle().addObserver(selfVM);
        this.getLifecycle().addObserver(billingVM);

        mAuth = FirebaseAuth.getInstance();
        user1 = mAuth.getCurrentUser();

        userReference = FirebaseDatabase.getInstance().getReference("User");

        chatReference = FirebaseDatabase.getInstance().getReference("Chat");

        me = chatReference.child(user1.getUid());
        myFriends = me.child("Friends");

        //myStatus = me.child("Status");

        selfVM.getIsPremiumPurchased().observe(this, isPremiumPurchasedObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        menu.clear();
        menu.add(Menu.NONE, 0, Menu.NONE, "Fire Alert").setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.baseline_local_fire_department_24)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, 1, Menu.NONE, "Nearby Alert").setIcon(ContextCompat.getDrawable(getBaseContext(), R.drawable.baseline_location_on_24)).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(getBaseContext(), Fire.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            case 1:
                startActivity(new Intent(getBaseContext(), Nearby.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finishAffinity();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    private void initLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = getDate();//DateFormat.getTimeInstance().format(new Date());
                startLocationUpdates();
                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getBaseContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                locationAddress = addresses.get(0);
                getAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getAddress();

            mLocation = "Latitude: " + mCurrentLocation.getLatitude() + ", " +
                    "Longitude: " + mCurrentLocation.getLongitude() + "; \n" +
                    mAddress;
            mTime = mLastUpdateTime;

            LocationBase l = new LocationBase(mLocation, mTime);
            LocationBaseManager.saveLocation(getBaseContext(), l);
        }
        showLastKnownLocation();
    }

    public void getAddress() {
        String mstreet;

        if (locationAddress != null) {
            String street = locationAddress.getAddressLine(0);
            String locality = locationAddress.getLocality();
            String lga = locationAddress.getSubAdminArea();
            String state = locationAddress.getAdminArea();
            String country = locationAddress.getCountryName();

            if (!TextUtils.isEmpty(locality) && street.contains(locality)) {
                int int1 = street.lastIndexOf(",");
                String str1 = street.substring(0, int1);
                int int2 = str1.lastIndexOf(",");
                mstreet = str1.substring(0, int2);
            } else if (!TextUtils.isEmpty(country) && street.contains(country)) {
                int int1 = street.lastIndexOf(",");
                mstreet = street.substring(0, int1);
            } else mstreet = street;

            String currentLocation;

            if (!TextUtils.isEmpty(street)) {
                if (!mstreet.equals("Unnamed Road"))
                    currentLocation = mstreet + ",\n";
                else currentLocation = "";

                if (!TextUtils.isEmpty(locality))
                    currentLocation += "(locality: " + locality + "),\n";

                if (!TextUtils.isEmpty(lga))
                    currentLocation += lga + ", ";

                if (!TextUtils.isEmpty(state))
                    currentLocation += state + ", ";

                if (!TextUtils.isEmpty(country))
                    currentLocation += country + ".";

                mAddress = currentLocation;
            }
        } else
            //showToast("Something went wrong");
            mAddress = "#unspecified address!";
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }

    /**
     * Starting location updates
     * Check whether location settings are satisfied and then
     * location updates will be requested
     */
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");
                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(Self.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(Self.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    public void showLastKnownLocation() {
        if (mCurrentLocation != null) {
            textView.setAlpha(0);
            textView.animate().alpha(1).setDuration(300);
            textView.setText(mLocation + " On " + mTime);
        } else {
            textView.setAlpha(0);
            textView.animate().alpha(1).setDuration(300);
            // TODO: LOCATION .... Last known location is not available!
            textView.setText("Last known location is not available!");
        }
    }

    public void showMap() {
        if (mCurrentLocation != null) {
            Intent imap = new Intent(Self.this, MapsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putDouble("long", mCurrentLocation.getLongitude());
            bundle.putDouble("lat", mCurrentLocation.getLatitude());
            imap.putExtras(bundle);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            startActivity(imap);
        } else {
            //Toast.makeText(getBaseContext(), "Location null!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.v(LOG_TAG, "Receieved notification about network status");
            isNetworkAvailable(context);
        }

        private boolean isNetworkAvailable(@NotNull Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        if (!isConnected) {
                            Log.v(LOG_TAG, "Now you are connected to Internet!");
                            hideNetworkDialog();
                            startLocationUpdates();
                            //checkAppUpdate();

                            isConnected = true;
                            //do your processing here ---

                        }
                        return true;
                    }
                }
            }
            Log.v(LOG_TAG, "You are not connected to Internet!");
            showNetworkDialog();
            stopLocationUpdates();
            isConnected = false;
            return false;
        }
    }

}