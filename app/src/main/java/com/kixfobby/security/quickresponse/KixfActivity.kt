package com.kixfobby.security.quickresponse

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.kixfobby.security.localization.ui.LocalizationActivity
import com.kixfobby.security.permission.HiPermission
import com.kixfobby.security.permission.PermissionCallback
import com.kixfobby.security.quickresponse.helper.DateTimeHelper
import com.kixfobby.security.quickresponse.helper.UpdateDialog
import com.kixfobby.security.quickresponse.home.Self
import com.kixfobby.security.quickresponse.model.StudentInfo
import com.kixfobby.security.quickresponse.model.User
import com.kixfobby.security.quickresponse.storage.Pref
import org.jetbrains.annotations.Contract
import java.io.*
import java.lang.reflect.Method

open class KixfActivity : LocalizationActivity() {

    val versionCode = Build.VERSION_CODES.

    fun grantPermission(context: Activity, permission: Array<String>) {
        var activity = getActivity(context)
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission.toString())) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            toast(context, "Please provide app permission so that you can send sms")
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(context, permission, REQUEST_CODE)
        }
    }

    fun grantPermissions(context: Context) {
        /*HiPermission.create(context)
            .animStyle(R.style.PermissionAnimFade)
            .checkMutiPermission(object : PermissionCallback {
                override fun onClose() {
                    Log.i(TAG, "onClose")
                    toast(this@KixfActivity, "Permission denied!")
                }

                override fun onFinish() {
                    Log.i(TAG, getString(R.string.finish))
                }

                override fun onDeny(permission: String, position: Int) {
                    Log.i(TAG, "onDeny")
                }

                override fun onGuarantee(permission: String, position: Int) {
                    Log.i(TAG, "onGuarantee")
                }
            })*/
    }

    fun openSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }

    fun toast(context: Context, message: String) {
        var activity = getActivity(context)
        val layout =
            activity?.layoutInflater?.inflate(R.layout.custom_toast_layout, activity.findViewById(R.id.toast_container))

        val textView = layout?.findViewById<TextView>(R.id.toast_text)
        textView?.text = message

        // use the application extension function
        Toast(activity).apply {
            setGravity(Gravity.BOTTOM, 0, 40)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    fun editorEmpty(editText: EditText): Boolean {
        return editText.text.toString().trim { it <= ' ' }.length == 0
    }

    fun editorString(editText: EditText): String {
        return editText.text.toString().trim { it <= ' ' }
    }

    val date: String get() = DateTimeHelper().current

    fun showUpdate() {
        val s1 =
            Pref(this).get("vtitle", "Update Available")
        val s2 = Pref(this).get(
            "vdescription",
            "A new version of the application is available please click below to update to the latest version."
        )
        val s3 = Pref(this).get("vcode", "versionCode")
        val s4 = Pref(this).get("vforce", "versionCode")
        if (versionCode < s4) {
            val ud = UpdateDialog(this, s1, s2, s3, true)
            ud.setCancelable(true)
            ud.show()
        }
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    @Contract("null -> null")
    fun getActivity(context: Context?): Activity? {
        var context = context
        while (context !is Activity && context is ContextWrapper) {
            context = context.baseContext
        }
        return if (context is Activity) {
            context
        } /*else if (context instanceof android.app.Service) {
            return (android.app.Service) context;
        }*/ else null
    }

    fun checkService(serviceClass: Class<*>?) {
        val intent = Intent(applicationContext, serviceClass)
        if (PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_NO_CREATE) == null) {
            startService(intent)
        } else {
            //
            // .makeText(getApplicationContext(), "service is already running!", Toast.LENGTH_SHORT).show();
        }
    }

    fun saveUsers(c: Context, user: List<User>) {
        try {
            val fos = c.openFileOutput("users_db", MODE_PRIVATE)
            val oos = ObjectOutputStream(fos)
            oos.writeObject(user)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE NOT FOUND", Toast.LENGTH_SHORT).show();
        } catch (e: IOException) {
            //.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
        }
    }

    fun retrieveUsers(c: Context): List<User> {
        var ret: List<User> = ArrayList()
        try {
            val fis = c.openFileInput("users_db")
            val ois = ObjectInputStream(fis)
            ret = ois.readObject() as ArrayList<User>
        } catch (e: FileNotFoundException) {
            saveUsers(c, ArrayList())
        } catch (e: IOException) {
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
        } catch (e: ClassNotFoundException) {
            //Toast.makeText(c, "UNABLE TO SAVE USERS FILE", Toast.LENGTH_SHORT).show();
            e.printStackTrace()
        }
        return ret
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun autoSetOverlayPermission(context: Context, packageName: String) {
        val packageManager: PackageManager = context.getPackageManager()
        var uid = 0
        uid = try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            applicationInfo.uid
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return
        }
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val OP_SYSTEM_ALERT_WINDOW = 24
        try {
            val clazz: Class<*> = AppOpsManager::class.java
            val method: Method = clazz.getDeclaredMethod(
                "setMode",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java,
                Int::class.javaPrimitiveType
            )
            //method.invoke(appOpsManager, OP_SYSTEM_ALERT_WINDOW, uid, packageName, AppOpsManager.MODE_ALLOWED)
            Log.e(TAG, "Overlay permission granted to $packageName")
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
        }
    }

    fun stringToFile(context: Context, data: String): InputStream? {
        var stream: InputStream? = null
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("purchase_json.txt", MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
            stream = context.openFileInput("purchase_json.txt")

        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
        return stream
    }

    fun showHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("User Information for Payment")
            .setMessage(
                "How to use the app. \n\n This security App has lot and lot of features like Location, maps, camera, video, SMS, notification, Alarm etc. " +
                        "\n\nlocation takes of your present location at per time. you register 5 trusted number on the App. if any emergency and you press the finger botton it send SMS and notification to those 5 numbers you registered. " +
                        "\n\ncamera: it has hidden and open camera of which. the hidden is strictly when someone wants to open your phone without your authorization. the open camera snap and save it on your gallery, your cloud and on our cloud like wise the videos. " +
                        "\n\nEmergency number you can contact the nearest police station or fire fighter close to you if need be. " +
                        "\n\nAlarm: this works with notification and SMS. example when traveling on a far distance and the space is 8 hour, you can set the Alarm within the interval of one hour to ring and if it rings, you did dis-alarm it the first time, the second ringing it sends automatic SMS and notification to those numbers. " +
                        "\n\nmap: this has three different kinds of map and it tells you your location anytime and where. " +
                        "\n\nSMS: You can chat with it on notification with your data and send SMS with you phone recharge neither of them. " +
                        "\n\nlot of features."
            )
            .setView(R.layout.dialog_set_payment_details)
            .setCancelable(true)
            .setBackground(AppCompatResources.getDrawable(this, R.drawable.background_dialog))
            .setIcon(R.drawable.qr_logo360)
            .setPositiveButton(getString(R.string.okay)) { mInterface, which ->
                val email =
                    ((mInterface as? Dialog)!!.findViewById<View>(R.id.edt_email) as TextInputEditText).text.toString()

            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    fun showPaymentSuggestDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("User Information for Payment")
            .setMessage(
                "Save the data of the user to whom the payment will be associated at the end of the payment procedure. \n" +
                        "Please enter the user's correct information. \n" +
                        "Details can't be changed or removed once they've been saved."
            )
            .setView(R.layout.dialog_set_payment_details)
            .setCancelable(true)
            .setBackground(AppCompatResources.getDrawable(this, R.drawable.background_dialog))
            .setIcon(R.drawable.baseline_payment_24)
            .setPositiveButton(getString(R.string.okay)) { mInterface, which ->
                val email =
                    ((mInterface as? Dialog)!!.findViewById<View>(R.id.edt_email) as TextInputEditText).text.toString()
                val phone =
                    ((mInterface as? Dialog)!!.findViewById<View>(R.id.edt_phone) as TextInputEditText).text.toString()
                val mLayoutEmail =
                    ((mInterface as? Dialog)!!.findViewById<View>(R.id.layout_email) as TextInputLayout)
                val mLayoutPhone =
                    ((mInterface as? Dialog)!!.findViewById<View>(R.id.layout_phone) as TextInputLayout)

                when {
                    TextUtils.isEmpty(email) -> {
                        toast(this, "Please provide accurate information.")
                    }
                    TextUtils.isEmpty(phone) -> {
                        toast(this, "Please provide accurate information.")
                    }
                    email == "" || Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        toast(this, "Please, enter valid details.")
                    }
                    phone == "" -> {
                        toast(this, "Please, enter valid details.")
                    }
                    email.length <= 6 -> {
                        toast(this, "Please, enter valid details.")
                    }
                    phone.length <= 6 -> {
                        toast(this, "Please, enter valid details.")
                    }
                    else -> {

                    }
                }
            }
            .setNegativeButton(getString(android.R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /*fun readFromFile(context: Context): String? {
        var ret = ""
        try {
            val inputStream: InputStream? = context.openFileInput("purchase_json.txt")
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }
        return ret
    }
*/

    fun trims(str: String): String {
        return str.replace("\\s".toRegex(), "")
    }

    fun secToMil(seconds: Int): Long {
        return seconds * 1000L
    }

    fun minToMil(minutes: Int): Long {
        return minutes * 60000L
    }

    companion object {
        private const val REQUEST_CODE = 1

        fun verifyInstaller(context: Context): Boolean {
            val installer = context.packageManager.getInstallerPackageName(context.packageName)
            return installer != null && installer == "com.android.vending"
        }
    }

    fun fetchUser(): ArrayList<String>? {
        val user1 = FirebaseAuth.getInstance().currentUser
        var resList: ArrayList<String> = ArrayList<String>()
        for (email in getAllPaidUsers()) {
            if (user1!!.email.equals(email)) {
                resList.add(email)
            }
        }
        return resList
    }

    fun checkPaidUser() {
        val user1 = FirebaseAuth.getInstance().currentUser!!
        if (fetchUser()?.size!! > 0) {
            if (fetchUser()?.get(0).equals(user1.email)) Pref(this).put("checkPaidUser", true)
        } else Pref(this).put("checkPaidUser", false)
        //showPaymentSuggestDialog()


        if (user1.email.equals(Self.ad1, ignoreCase = true) ||
            user1.email.equals(Self.ad2, ignoreCase = true) ||
            user1.email.equals(Self.ad3, ignoreCase = true) ||
            user1.email.equals(Self.ad4, ignoreCase = true) ||
            user1.email.equals(Self.ad5, ignoreCase = true) ||
            user1.email.equals(Self.ad6, ignoreCase = true) ||
            user1.email.equals(Self.ad7, ignoreCase = true) ||
            user1.email.equals(Self.ad8, ignoreCase = true) ||
            user1.email.equals(Self.ad9, ignoreCase = true) ||
            user1.email.equals(Self.ad10, ignoreCase = true)
        ) Pref(this).put("checkPaidUser", true)
    }

    fun getAllPaidUsers(): ArrayList<String> {
        var valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list: MutableList<String?> = java.util.ArrayList()
                for (ds in dataSnapshot.children) {
                    val a: String? = ds.getValue(String::class.java)
                    list.add(a!!)
                }
                val set: MutableSet<String?> = HashSet()
                set.addAll(list)
                Pref(this@KixfActivity).put("paid_users", set)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //throw databaseError.toException() // never ignore errors
            }
        }
        FirebaseDatabase.getInstance().getReference("Purchase").child("ALL")
            .addValueEventListener(valueEventListener as ValueEventListener)

        val set: Set<String>? = Pref(this).get("paid_users", setOf("Paid Users"))
        val list2: ArrayList<String> = ArrayList()
        list2.addAll(set!!)

        return list2
    }


    public fun writeDataOnFirestore(phone: String, email: String) {
        var mFirestore: FirebaseFirestore? = null
        val map = HashMap<String, String>()
        map[phone] = email

        mFirestore = FirebaseFirestore.getInstance()
        mFirestore.collection("payment_info").document("payment_list").set(map, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    public fun writeDataOnFirestore(studentInfo: StudentInfo) {
        var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        mFirestore.collection("student_info").document("student_list")
            .set(studentInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "DocumentSnapshot successfully written!", Toast.LENGTH_LONG).show()
                Log.d(TAG, "DocumentSnapshot successfully written!")

                //initListView()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error writing document", e)
            }
    }

    public fun readDataFromFirestore(): ArrayList<String>? {
        val studentItemList = mutableListOf<String>()
        var immutableList = ArrayList<String>()
        var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        // Source can be CACHE, SERVER, or DEFAULT.
        val source = Source.CACHE

        // Get the document, forcing the SDK to use the offline cache
        mFirestore
            .collection("payment_info").document("payment_list")
            .get()
            .addOnSuccessListener { result ->
                for (doc in listOf(result.data)) {
                    var itm = doc?.values
                    //studentItemList.add(studentItem!!)
                    immutableList.add(itm!!.toString())

                    //toast(this, immutableList.get(0).toString())
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
            /*.addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)


                addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Document found in the offline cache
                        val document = task.result
                        //try {
                        if (document != null) {
                            // Document found in the offline cache

                            studentInfo = document.toObject<StudentInfo>()
                            toast(this, studentInfo!!.studentList.get(0).mobile_no)
                            //initListView()
                            Toast.makeText(this, "DocumentSnapshot read successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "No such document!", Toast.LENGTH_LONG).show()
                        }
                        } catch (ex: Exception) {
                        Log.e(TAG, ex.message!!)
                        toast(this, ex.message!!)
                }
            }*/.addOnFailureListener { e ->
                Log.e(TAG, "Error writing document", e)
                toast(this, "Error writing document")
            }
        return immutableList
    }

    public fun readDataFromFirestore(ageCondition: Int) {
        var mFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()

        mFirestore
            .collection("student_info")
            .whereLessThan("age", ageCondition)
            .get()
            .addOnSuccessListener { documents ->
                try {
                    if (documents != null) {
                        for (document in documents) {
                            Log.d(TAG, "${document.id} => ${document.data}")
                        }
                        Toast.makeText(this, "DocumentSnapshot read successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "No such document!", Toast.LENGTH_LONG).show()
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, ex.message!!)
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error writing document", e)
            }
    }
}