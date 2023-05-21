package com.kixfobby.security.quickresponse.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.storage.Pref
import com.kixfobby.security.quickresponse.ui.PaidUsersActivity
import com.kixfobby.security.quickresponse.ui.StorageActivity


/**
 * Service to handle uploading files to Firebase Storage.
 */
class MyUploadService : MyBaseTaskService() {

    private var sessionUri: Uri? = null
    private var pathString: String? = null

    // [START declare_ref]
    private lateinit var storageRef: StorageReference
    // [END declare_ref]

    override fun onCreate() {
        super.onCreate()

        // [START get_storage_ref]
        storageRef = Firebase.storage.reference
        // [END get_storage_ref]
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand:$intent:$startId")
        if (ACTION_UPLOAD == intent.action) {
            val uri = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)
            val file = intent.getParcelableExtra<Uri>(EXTRA_FILE_URI)
            // Make sure we have permission to read the data
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                contentResolver.
                takePersistableUriPermission(
                    fileUri!!,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }*/

            var format = Pref(this).get("fileFormat", "fileFormat").toString()
            pathString = format
            if (format == "text") {
                uploadFromFile(file!!)
                Pref(this).put("isPaymentMade", true)

                var i = Intent(this, PaidUsersActivity::class.java)
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(i)
            } else if (format == "image") {
                uploadFromUri(uri!!)
            } else if (format == "video") {
                uploadFromUri(uri!!)
            }
        }

        return Service.START_REDELIVER_INTENT
    }

    private fun uploadFromUri(mUri: Uri) {
        Log.d(TAG, "uploadFromUri:src:" + mUri.toString())
        taskStarted()
        /////showProgressNotification(getString(R.string.progress_uploading), 0, 0)
        val getUid = Pref(this).get("uid", "uid")
        val metadata = StorageMetadata.Builder().setCustomMetadata("Payment", "product purchase data").build()
        mUri.lastPathSegment?.let {
            val photoRef = storageRef.child(pathString!!).child(getUid.toString())
                .child(it)
            Log.d(TAG, "uploadFromUri:dst:" + photoRef.path)
            photoRef.putFile(mUri, metadata).addOnProgressListener { taskSnapshot ->
                sessionUri = taskSnapshot.uploadSessionUri
                if (sessionUri != null) {
                    Pref(baseContext).put("sessionUri", sessionUri.toString())
                }
                /////showProgressNotification(getString(R.string.progress_uploading), taskSnapshot.bytesTransferred, taskSnapshot.totalByteCount)
            }.continueWithTask { task ->
                // Forward any exceptions
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                Log.d(TAG, "uploadFromUri: upload success")
                photoRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                Log.d(TAG, "uploadFromUri: getDownloadUri success")
                broadcastUploadFinished(downloadUri, mUri)
                /////showUploadFinishedNotification(downloadUri, fileUri)
                taskCompleted()
            }.addOnFailureListener { exception ->
                Log.w(TAG, "uploadFromUri:onFailure", exception)
                broadcastUploadFinished(null, mUri)
                /////showUploadFinishedNotification(null, fileUri)
                taskCompleted()
            }
        }
    }

    private fun uploadFromFile(mFile: Uri) {
        Log.d(TAG, "uploadFromUri:src:" + mFile.toString())
        taskStarted()

        val getUid = Pref(this).get("uid", "uid")
        mFile.lastPathSegment?.let {
            val fileRef = storageRef.child("payments").child(getUid.toString())
                .child(it)
            fileRef.putFile(mFile).addOnProgressListener { taskSnapshot ->
                sessionUri = taskSnapshot.uploadSessionUri
                if (sessionUri != null) {
                    Pref(baseContext).put("sessionUri", sessionUri.toString())
                }
            }.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                Log.d(TAG, "uploadFromUri: upload success")
                fileRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                Log.d(TAG, "uploadFromUri: getDownloadUri success")
                broadcastUploadFinished(downloadUri, mFile)
                taskCompleted()
            }.addOnFailureListener { exception ->
                Log.w(TAG, "uploadFromUri:onFailure", exception)
                broadcastUploadFinished(null, mFile)
                taskCompleted()
            }
        }
    }


    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private fun broadcastUploadFinished(downloadUrl: Uri?, fileUri: Uri?): Boolean {
        val success = downloadUrl != null

        val action = if (success) UPLOAD_COMPLETED else UPLOAD_ERROR

        val broadcast = Intent(action)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
        return LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(broadcast)
    }

    /**
     * Show a notification for a finished upload.
     */
    private fun showUploadFinishedNotification(downloadUrl: Uri?, fileUri: Uri?) {
        // Hide the progress notification
        /////dismissProgressNotification()

        // Make Intent to StorageActivity
        val intent = Intent(this, StorageActivity::class.java)
            .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
            .putExtra(EXTRA_FILE_URI, fileUri)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        val success = downloadUrl != null
        val caption = if (success) getString(R.string.upload_success) else getString(R.string.upload_failure)
        /////showFinishedNotification(caption, intent, success)
    }

    companion object {

        private const val TAG = "MyUploadService"

        /** Intent Actions  */
        const val ACTION_UPLOAD = "action_upload"
        const val UPLOAD_COMPLETED = "upload_completed"
        const val UPLOAD_ERROR = "upload_error"

        /** Intent Extras  */
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_DOWNLOAD_URL = "extra_download_url"

        val intentFilter: IntentFilter
            get() {
                val filter = IntentFilter()
                filter.addAction(UPLOAD_COMPLETED)
                filter.addAction(UPLOAD_ERROR)

                return filter
            }
    }
}
