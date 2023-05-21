package com.kixfobby.security.quickresponse.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.CameraController
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.databinding.StorageMainBinding
import com.kixfobby.security.quickresponse.service.MyDownloadService
import com.kixfobby.security.quickresponse.service.MyUploadService
import java.io.File
import java.util.*

/**
 * Activity to upload and download photos from Firebase Storage.
 *
 * See [MyUploadService] for upload example.
 * See [MyDownloadService] for download example.
 */
class StorageActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var auth: FirebaseAuth

    private var downloadUrl: Uri? = null
    private var fileUri: Uri? = null

    private lateinit var binding: StorageMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StorageMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Click listeners
        with(binding) {
            buttonCamera.setOnClickListener(this@StorageActivity)
            buttonSignIn.setOnClickListener(this@StorageActivity)
            buttonDownload.setOnClickListener(this@StorageActivity)
        }

        // Local broadcast receiver
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.d(TAG, "onReceive:$intent")
                hideProgressBar()

                when (intent.action) {
                    MyDownloadService.DOWNLOAD_COMPLETED -> {
                        // Get number of bytes downloaded
                        val numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0)

                        // Alert success
                        showMessageDialog(
                            getString(R.string.success), String.format(
                                Locale.getDefault(),
                                "%d bytes downloaded from %s",
                                numBytes,
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)
                            )
                        )
                    }
                    MyDownloadService.DOWNLOAD_ERROR ->
                        // Alert failure
                        showMessageDialog(
                            "Error", String.format(
                                Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)
                            )
                        )
                    MyUploadService.UPLOAD_COMPLETED, MyUploadService.UPLOAD_ERROR -> onUploadResultIntent(intent)
                }
            }
        }

        // Restore instance state
        savedInstanceState?.let {
            fileUri = it.getParcelable(KEY_FILE_URI)
            downloadUrl = it.getParcelable(KEY_DOWNLOAD_URL)
        }
        onNewIntent(intent)
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)

        // Register receiver for uploads and downloads
        val manager = LocalBroadcastManager.getInstance(this)
        manager.registerReceiver(broadcastReceiver, MyDownloadService.intentFilter)
        manager.registerReceiver(broadcastReceiver, MyUploadService.intentFilter)
    }

    public override fun onStop() {
        super.onStop()

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }

    @SuppressLint("MissingSuperCall")
    public override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putParcelable(KEY_FILE_URI, fileUri)
        out.putParcelable(KEY_DOWNLOAD_URL, downloadUrl)
    }

    private fun hasCamera(): Boolean {
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
        ) {
            true
        } else {
            false
        }
    }

    fun openCamera() {
        if (hasCamera()) {
            val mediaFile =
                File(Environment.getExternalStorageDirectory().absolutePath + "/Android/media/com.kixfobby.security.quickresponse/" + "myvideo.mp4")
            val videoUri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", mediaFile)

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            startActivityForResult(intent, CameraController.VIDEO_CAPTURE)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult:$requestCode:$resultCode:$data")
        when (requestCode) {

            CameraController.VIDEO_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    fileUri = data?.data!!
                    uploadFromUri(fileUri!!)
                    //val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

                    Toast.makeText(this, "Video saved to: $fileUri", Toast.LENGTH_LONG).show()
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed to record video", Toast.LENGTH_LONG).show()
                }
            }

            RC_TAKE_PICTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    fileUri = data?.data

                    if (fileUri != null) {
                        //uploadFromUri(fileUri!!)
                    } else {
                        Log.w(TAG, "File URI is null")
                    }
                } else {
                    Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadFromUri(uploadUri: Uri) {
        Log.d(TAG, "uploadFromUri:src: $uploadUri")

        // Save the File URI
        fileUri = uploadUri

        // Clear the last download, if any
        updateUI(auth.currentUser)
        downloadUrl = null

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(
            Intent(this, MyUploadService::class.java)
                .putExtra(MyUploadService.EXTRA_FILE_URI, uploadUri)
                .setAction(MyUploadService.ACTION_UPLOAD)
        )

        // Show loading spinner
        showProgressBar(getString(R.string.progress_uploading))
    }

    private fun beginDownload() {
        fileUri?.let {
            // Get path
            val path = "photos/" + it.lastPathSegment

            // Kick off MyDownloadService to download the file
            val intent = Intent(this, MyDownloadService::class.java)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD)
            startService(intent)

            // Show loading spinner
            showProgressBar(getString(R.string.progress_downloading))
        }
    }

    private fun launchCamera() {
        Log.d(TAG, "launchCamera")

        // Pick an image from storage
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        startActivityForResult(intent, RC_TAKE_PICTURE)
    }

    private fun signInAnonymously() {
        // Sign in anonymously. Authentication is required to read or write from Firebase Storage.
        showProgressBar(getString(R.string.progress_auth))
        auth.signInAnonymously()
            .addOnSuccessListener(this) { authResult ->
                Log.d(TAG, "signInAnonymously:SUCCESS")
                hideProgressBar()
                updateUI(authResult.user)
            }
            .addOnFailureListener(this) { exception ->
                Log.e(TAG, "signInAnonymously:FAILURE", exception)
                hideProgressBar()
                updateUI(null)
            }
    }

    private fun onUploadResultIntent(intent: Intent) {
        // Got a new intent from MyUploadService with a success or failure
        downloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL)
        fileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI)

        updateUI(auth.currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        with(binding) {
            // Signed in or Signed out
            if (user != null) {
                layoutSignin.visibility = View.GONE
                layoutStorage.visibility = View.VISIBLE
            } else {
                layoutSignin.visibility = View.VISIBLE
                layoutStorage.visibility = View.GONE
            }

            // Download URL and Download button
            if (downloadUrl != null) {
                pictureDownloadUri.text = downloadUrl.toString()
                layoutDownload.visibility = View.VISIBLE
            } else {
                pictureDownloadUri.text = null
                layoutDownload.visibility = View.GONE
            }
        }
    }

    private fun showMessageDialog(title: String, message: String) {
        val ad = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .create()
        ad.show()
    }

    private fun showProgressBar(progressCaption: String) {
        with(binding) {
            caption.text = progressCaption
            progressBar.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        with(binding) {
            caption.text = ""
            progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        return /*if (i == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut()
            updateUI(null)
            true
        } else {
            super.onOptionsItemSelected(item)
        }*/true
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonCamera -> openCamera()
            //R.id.buttonCamera -> launchCamera()
            R.id.buttonSignIn -> signInAnonymously()
            R.id.buttonDownload -> beginDownload()
        }
    }

    companion object {

        private const val TAG = "Storage#StorageActivity"

        private const val RC_TAKE_PICTURE = 101

        private const val KEY_FILE_URI = "key_file_uri"
        private const val KEY_DOWNLOAD_URL = "key_download_url"
    }
}
