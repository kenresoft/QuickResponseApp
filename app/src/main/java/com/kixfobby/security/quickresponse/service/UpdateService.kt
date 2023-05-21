package com.kixfobby.security.quickresponse.service;

import android.app.*
import android.app.Notification.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.kixfobby.security.quickresponse.*
import com.kixfobby.security.quickresponse.BuildConfig
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.helper.alarm.NotificationScheduler
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.model.Message
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.model.ViewHolderChat
import com.kixfobby.security.quickresponse.receiver.Restarter
import com.kixfobby.security.quickresponse.storage.*
import com.kixfobby.security.quickresponse.widget.AlarmTimer
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit


class UpdateService : Service(), AlarmTimer.OnCountDownListener {

    //private var session: SharedPreferences? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private var isAlarmStarted = false

    private val IDENTIFIER = "getentService"
    private val FB_RC_KEY_TITLE = "update_title"
    private val FB_RC_KEY_DESCRIPTION = "update_description"
    private val FB_RC_KEY_FORCE_UPDATE_VERSION = "force_update_version"
    private val FB_RC_KEY_LATEST_VERSION = "latest_version"
    private val FB_RC_KEY_ADMIN_1 = "admin1"
    private val FB_RC_KEY_ADMIN_2 = "admin2"
    private val FB_RC_KEY_ADMIN_3 = "admin3"
    private val FB_RC_KEY_ADMIN_4 = "admin4"
    private val FB_RC_KEY_ADMIN_5 = "admin5"
    private val FB_RC_KEY_ADMIN_6 = "admin6"
    private val FB_RC_KEY_ADMIN_7 = "admin7"
    private val FB_RC_KEY_ADMIN_8 = "admin8"
    private val FB_RC_KEY_ADMIN_9 = "admin9"
    private val FB_RC_KEY_ADMIN_10 = "admin10"


    companion object {
        val TAG: String? = "UpdateService"
        var notifyMesasage = "Please wait..."
    }

    private var FB_RC_KEY_USER_MESSAGE: String? = null
    private val FB_RC_KEY_ADMIN_MESSAGE = "admin_message"

    val versionCode = BuildConfig.VERSION_CODE
    private var databaseReference: DatabaseReference? = null
    private var reference1: DatabaseReference? = null
    private var me: DatabaseReference? = null
    private var you: DatabaseReference? = null
    private var myFriends: DatabaseReference? = null
    private var yourFriends: DatabaseReference? = null
    private var myStatus: DatabaseReference? = null
    private var mFriend: DatabaseReference? = null
    private var yFriend: DatabaseReference? = null
    private var myMessageDate: DatabaseReference? = null
    private var yourMessageDate: DatabaseReference? = null
    private var chatReference: DatabaseReference? = null
    private var userReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null
    private val mAuthListener: AuthStateListener? = null
    private var user1: FirebaseUser? = null
    private val childEventListener2: ChildEventListener? = null
    private val childEventListener1: ValueEventListener? = null
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private val addressResultReceiver: ResultReceiver? = null
    private val firebaseRecyclerAdapter: FirebaseRecyclerAdapter<Message, ViewHolderChat>? = null
    private lateinit var storageRef: StorageReference
    private var i: Int = 0

    private var mCurrentService: UpdateService? = null
    private var counter = 0

    private var duration: Long = 0
    private lateinit var countDownTimer: AlarmTimer


    override fun onBind(intent: Intent): IBinder? {
        //log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "restarting Service !!")

        // make sure you call the startForeground on onStartCommand because otherwise
        // when we hide the notification on onScreen it will nto restart in Android 6 and 7
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val notification = createNotification()
            startForeground(1, notification)
        }

        if (intent != null) {
            val action = intent.action
            //log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                //else -> log("This should never happen. No action in the received intent")
            }
        } else {
            //log("with a null intent. It has been probably restarted by the system.")
            // it has been killed by Android and now it is restarted. We must make sure to have reinitialised everything
            val bck = ProcessMainClass()
            bck.launchService(this)
        }
        startTimer()
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        Pref(baseContext).put("alarm_duration", 120L)
        duration = Pref(baseContext).get("alarm_duration", 120L)
        countDownTimer = AlarmTimer(0, duration, this)

        val notification = createNotification()
        startForeground(1, notification)
        storageRef = Firebase.storage.reference
        mCurrentService = this
    }

    override fun onDestroy() {
        super.onDestroy()
        val broadcastIntent1 = Intent(this, Restarter::class.java)
        sendBroadcast(broadcastIntent1)
        NotificationScheduler.setRestarter(this, Restarter::class.java, false, 0)

        val broadcastIntent2 = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent2)

        stoptimertask()
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val broadcastIntent1 = Intent(this, Restarter::class.java)
        sendBroadcast(broadcastIntent1)
        NotificationScheduler.setRestarter(this, Restarter::class.java, false, 0)

        val broadcastIntent2 = Intent(Constants.RESTART_INTENT)
        sendBroadcast(broadcastIntent2)

        val restartServiceIntent = Intent(applicationContext, UpdateService::class.java).also {
            it.setPackage(packageName)
        }

        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)

        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    @DelicateCoroutinesApi
    private fun startService() {

        if (isServiceStarted) return
        if (isAlarmStarted) return
        //log("Starting the foreground service task")
        //.makeText(this, "Service starting its task")
        isServiceStarted = true
        isAlarmStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UpdateService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isAlarmStarted) {
                launch(Dispatchers.IO) {
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(Runnable { // Run your task here
                        //BaseActivity().toast(this@UpdateService, "ALARM ALERT BEEP!")
                        //with(countDownTimer) { start() }
                    }, 1)
                }
                delay((duration + 1) * 1000L)
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    // PUT YOUR CODE HERE
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed(Runnable { // Run your task here

                        Log.d(UpdateService.TAG, "UpdateService Running")
                        NotificationScheduler.setRestarter(baseContext, Restarter::class.java, false, 0)
                        startService(Intent(this@UpdateService, BaseService::class.java))
                        //Toast.makeText(this@UpdateService, "Active", Toast.LENGTH_SHORT).show()
                        val endTime = Pref(this@UpdateService).get("endTime", 1440L)
                        val startTime = Pref(this@UpdateService).get("startTime", 1440L)

                        if (System.currentTimeMillis() >= endTime) {
                            NotificationScheduler.cancelReminder(this@UpdateService)
                            NotificationScheduler.cancelNotification(this@UpdateService)
                            //Toast.makeText(baseContext, "Alarm ended...", Toast.LENGTH_SHORT).show()
                        }
                    }, 1)

                    checkAppUpdate()
                    checkUserUpdate()

                    ///// FIREBASE BACKGROUND TASKS
                    mAuth = FirebaseAuth.getInstance()
                    user1 = mAuth!!.currentUser

                    userReference = FirebaseDatabase.getInstance().getReference("User")
                    chatReference = FirebaseDatabase.getInstance().getReference("Chat")

                    var sessionUri: Uri? = Pref(baseContext).get("sessionUri", "sessionUri")?.toUri()
                    val photoRef = storageRef.child("photos")
                    val file =
                        File(BaseActivity.Companion.storageLocation + Pref(baseContext).put("file", "file"))
                    val localFile = Uri.fromFile(file)
                    /////session!!.contains("sessionUri")

                    if (i == 0) {
                        i++
                        if (file.exists()) {
                            photoRef.putFile(localFile, storageMetadata { }, sessionUri)
                                .addOnProgressListener { taskSnapshot ->

                                    sessionUri = taskSnapshot.uploadSessionUri
                                    if (sessionUri != null) {
                                        Pref(baseContext).put("sessionUri", sessionUri.toString())
                                    }
                                }
                                .continueWithTask { task ->
                                    // Forward any exceptions
                                    if (!task.isSuccessful) {
                                        throw task.exception!!
                                    }

                                    Log.d(UpdateService.TAG, "uploadFromUri: upload success")

                                    // Request the public download URL
                                    photoRef.downloadUrl
                                }
                                .addOnSuccessListener { downloadUri ->
                                    Pref(baseContext).remove("sessionUri")
                                    Log.d(UpdateService.TAG, "uploadFromUri: getDownloadUri success")
                                    i = 0
                                    Log.e(UpdateService.TAG, "i-success == 0")
                                }.addOnFailureListener { exception ->
                                    Log.w(UpdateService.TAG, "uploadFromUri:onFailure", exception)
                                    i = -1
                                    Log.e(UpdateService.TAG, "i-error == 0")
                                }
                        }
                    }

                    val it: List<User> = retrieveUsers(this@UpdateService)
                    for (i in it.indices) {
                        val fri = it[i].uid
                        if (user1 != null) {
                            me = chatReference!!.child(user1!!.uid)
                            //you = chatReference.child(fri);
                            myFriends = me!!.child("Friends")
                            //yourFriends = you.child("Friends");
                            mFriend = myFriends!!.child(fri!!)
                            //yFriend = yourFriends.child(user1.getUid());
                            val query: Query = mFriend!!.limitToLast(1)
                            val childEventListener: ChildEventListener = object : ChildEventListener {
                                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                                    // ...
                                    val value = dataSnapshot.getValue(
                                        Message::class.java
                                    )
                                    if (value != null) {
                                        if (value.messageType == "Inbox") {
                                            //toast(getApplicationContext(), (value.getMessage() + " From " + value.getSenderName()));
                                            notifyMesasage = value.message + " From " + value.senderName
                                            Pref(baseContext).put("nvCount", 0)
                                        }
                                    }
                                }

                                override fun onChildChanged(
                                    dataSnapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {
                                    // ...
                                    //toast(getApplicationContext(), "Child changed");
                                }

                                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                                    // ...
                                }

                                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                                    // ...
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // ...
                                }
                            }
                            query.addChildEventListener(childEventListener)
                        }
                    }
                }
                delay(3000L)
            }
            //log("End of the loop for the service")
        }
    }

    private fun stopService() {
        //log("Stopping the foreground service")
        //toast(this, "Service stopping")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            //log("Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        isAlarmStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    override fun onCountDownActive(time: String) {
        Toast.makeText(
            this,
            "Seconds = " + countDownTimer.getSecondsTillCountDown() + " Minutes=" + countDownTimer.getMinutesTillCountDown(),
            Toast.LENGTH_SHORT
        ).show()

    }

    override fun onCountDownFinished() {
        val notification = createNotification()
        startForeground(1, notification)
    }

    fun checkAppUpdate() {
        mAuth = FirebaseAuth.getInstance()
        user1 = mAuth!!.currentUser
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        userReference = FirebaseDatabase.getInstance().getReference("User")
        val defaultMap = HashMap<String, Any>()
        defaultMap[FB_RC_KEY_TITLE] = "Update Available"
        defaultMap[FB_RC_KEY_DESCRIPTION] =
            "A new version of the application is available please click below to update the latest version."
        defaultMap[FB_RC_KEY_FORCE_UPDATE_VERSION] = "" + versionCode
        defaultMap[FB_RC_KEY_LATEST_VERSION] = "" + versionCode
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1500).build()
        mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig!!.setDefaultsAsync(defaultMap)
        mFirebaseRemoteConfig!!.fetch(if (BuildConfig.DEBUG) 0 else TimeUnit.HOURS.toSeconds(4))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig!!.activate()

                    val title = getValue(FB_RC_KEY_TITLE, defaultMap)
                    val description = getValue(FB_RC_KEY_DESCRIPTION, defaultMap)
                    val forceUpdateVersion =
                        getValue(FB_RC_KEY_FORCE_UPDATE_VERSION, defaultMap)!!.toInt()
                    val latestAppVersion = getValue(FB_RC_KEY_LATEST_VERSION, defaultMap)
                    val message = FB_RC_KEY_USER_MESSAGE?.let { getValue(it, defaultMap) }
                    val admMsg = getValue(FB_RC_KEY_ADMIN_MESSAGE, defaultMap)

                    val adm1 = getValue(FB_RC_KEY_ADMIN_1, defaultMap)
                    val adm2 = getValue(FB_RC_KEY_ADMIN_2, defaultMap)
                    val adm3 = getValue(FB_RC_KEY_ADMIN_3, defaultMap)
                    val adm4 = getValue(FB_RC_KEY_ADMIN_4, defaultMap)
                    val adm5 = getValue(FB_RC_KEY_ADMIN_5, defaultMap)
                    val adm6 = getValue(FB_RC_KEY_ADMIN_6, defaultMap)
                    val adm7 = getValue(FB_RC_KEY_ADMIN_7, defaultMap)
                    val adm8 = getValue(FB_RC_KEY_ADMIN_8, defaultMap)
                    val adm9 = getValue(FB_RC_KEY_ADMIN_9, defaultMap)
                    val adm10 = getValue(FB_RC_KEY_ADMIN_10, defaultMap)

                    Pref(baseContext).put("vtitle", title)
                    Pref(baseContext).put("vdescription", description)
                    Pref(baseContext).put("vcode", latestAppVersion)
                    Pref(baseContext).put("vforce", forceUpdateVersion)
                    Pref(baseContext).put("vmessage", message)
                    Pref(baseContext).put("vadminmsg", admMsg)
                    Pref(baseContext).put("vadmin1", adm1)
                    Pref(baseContext).put("vadmin2", adm2)
                    Pref(baseContext).put("vadmin3", adm3)
                    Pref(baseContext).put("vadmin4", adm4)
                    Pref(baseContext).put("vadmin5", adm5)
                    Pref(baseContext).put("vadmin6", adm6)
                    Pref(baseContext).put("vadmin7", adm7)
                    Pref(baseContext).put("vadmin8", adm8)
                    Pref(baseContext).put("vadmin9", adm9)
                    Pref(baseContext).put("vadmin10", adm10)

                    if (user1 != null) {
                        FB_RC_KEY_USER_MESSAGE = user1!!.uid
                        defaultMap[FB_RC_KEY_USER_MESSAGE!!] = "" + "New Message for you"
                        reference1 = databaseReference!!.child(user1!!.uid)
                        reference1!!.child("Language").setValue(Pref(baseContext).get("lang", "langKeyFull"))
                        reference1!!.child("Country").setValue(Pref(baseContext).get("ctry", "co"))
                        reference1!!.child("State").setValue(Pref(baseContext).get("state", "state"))
                        reference1!!.child("Zip").setValue(Pref(baseContext).get("zip", "zip"))
                        reference1!!.child("Phone").setValue(Pref(baseContext).get("phone", "phone"))
                        reference1!!.child("App Version").setValue(BuildConfig.VERSION_CODE)
                    }
                } else {
                    //toast(getBaseContext(), "Fetch Failed");
                }
            }
    }

    fun checkUserUpdate() {
        val set: Set<String>? = Pref(baseContext).get("friends_set", setOf("Friend Set"))
        val mainList: MutableList<String> = ArrayList()
        if (set != null) {
            mainList.addAll(set)
        }
        //toast(UpdateService.this, String.valueOf(mainList.size()) + " users");
        val userList: MutableList<User> = ArrayList()
        for (i in mainList.indices) {
            val uid = mainList[i]
            val ref1: DatabaseReference = userReference!!.child(uid)
            ref1.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var name: String? = null
                    var email: String? = null
                    var phone: String? = null
                    for (ds in dataSnapshot.children) {
                        val genericTypeIndicator: GenericTypeIndicator<Map<String?, Any?>?> =
                            object : GenericTypeIndicator<Map<String?, Any?>?>() {}
                        val map = dataSnapshot.getValue(genericTypeIndicator)!!
                        name = (map["Name"] ?: "No name").toString()
                        email = (map["Email"] ?: "No email").toString()
                        phone = (map["Phone"] ?: "No phone").toString()
                    }
                    //toast(getBaseContext(), name + " " + uid);
                    val user = User().setName(name).setEmail(email).setPhone(phone).setUid(uid)
                    if (user1 != null) {
                        if (user.uid != user1!!.uid) {
                            userList.add(user)
                        }
                        saveUsers(applicationContext, userList)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    databaseError.toException()
                    //toast(UpdateService.this, databaseError.getMessage());
                }
            })
            /*Gson gson = new Gson();
            String json = gson.toJson(userList);
            PreferenceManager.getDefaultSharedPreferences(UpdateService.this).edit().putString("user_list", json).apply();*/
        }
        addUserListner()
    }

    fun addUserListner() {
        userReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(parent: DataSnapshot) {
                val list: MutableList<String?> = ArrayList()
                for (child in parent.children) {
                    val value = child.key
                    list.add(value)
                }
                val set: MutableSet<String?> = HashSet()
                set.addAll(list)
                Pref(baseContext).put("friends_set", set)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //toast(getApplicationContext(), databaseError.getMessage());
            }
        })
    }


    fun getValue(parameterKey: String, defaultMap: HashMap<String, Any>): String? {
        var value: String? = mFirebaseRemoteConfig!!.getString(parameterKey)
        if (TextUtils.isEmpty(value)) value = defaultMap[parameterKey] as String?
        return value
    }


    fun saveUsers(c: Context, user: List<User>?) {
        try {
            val fos = c.openFileOutput("users_db", MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(user)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //toast(c, "UNABLE TO SAVE USERS FILE NOT FOUND");
        } catch (e: IOException) {
            //toast(c, "UNABLE TO SAVE USERS FILE");
        }
    }

    fun retrieveUsers(c: Context): List<User> {
        var ret: List<User> = ArrayList()
        try {
            val fis = c.openFileInput("users_db")
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<User>
            for (u in ret) {
                //if (u.getUid().trim().equals(user1.getUid())) {
                ret.remove<Serializable?>(u.uid)
                //}
            }
            //toast(c, String.valueOf(ret.size()));
        } catch (e: FileNotFoundException) {
            saveUsers(applicationContext, ArrayList())
        } catch (e: IOException) {
            //toast(c, "UNABLE TO SAVE USERS FILE");
        } catch (e: ClassNotFoundException) {
            //toast(c, "UNABLE TO SAVE USERS FILE");
            e.printStackTrace()
        }
        return ret
    }


    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    var oldTime: Long = 0

    fun startTimer() {
        Log.i(TAG, "Starting timer")

        //set a new Timer - if one is already running, cancel it to avoid two running at the same time
        stoptimertask()
        timer = Timer()

        //initialize the TimerTask's job
        initializeTimerTask()
        Log.i(TAG, "Scheduling...")
        //schedule the timer, to wake up every 1 second
        timer!!.schedule(timerTask, 1000, 1000) //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    fun initializeTimerTask() {
        Log.i(TAG, "initialising TimerTask")
        timerTask = object : TimerTask() {
            override fun run() {
                Log.i("in timer", "in timer ++++  " + counter++)
            }
        }
    }

    /**
     * not needed
     */
    fun stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun getmCurrentService(): UpdateService? {
        return mCurrentService
    }

    fun setmCurrentService(mCurrentService: UpdateService) {
        this.mCurrentService = mCurrentService
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val notificationChannelId = "QRESPONSE_SERVICE_CHANNEL_ID"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "QResponse Active Service",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "QResponse Service Channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, Self::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                notificationChannelId
            ) else Notification.Builder(this)

        return builder
            //.setContentTitle("Running in background")
            .setContentText("Active")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.baseline_security_24)
            .setVisibility(VISIBILITY_SECRET)
            .build()
    }

}
