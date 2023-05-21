package com.kixfobby.security.quickresponse.service

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.androidhiddencamera.CameraConfig
import com.androidhiddencamera.CameraError
import com.androidhiddencamera.CameraError.CameraErrorCodes
import com.androidhiddencamera.HiddenCameraService
import com.androidhiddencamera.HiddenCameraUtils
import com.androidhiddencamera.config.CameraFacing
import com.androidhiddencamera.config.CameraFocus
import com.androidhiddencamera.config.CameraImageFormat
import com.androidhiddencamera.config.CameraResolution
import com.kixfobby.security.quickresponse.R
import java.io.File


class DemoCamService : HiddenCameraService() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            if (HiddenCameraUtils.canOverDrawOtherApps(this)) {
                val cameraConfig = CameraConfig()
                    .getBuilder(this)
                    .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                    .setCameraFocus(CameraFocus.AUTO)
                    .build()
                startCamera(cameraConfig)
                Handler().postDelayed({
                    //Toast.makeText( this@DemoCamService, "Capturing image.", Toast.LENGTH_SHORT ).show()
                    takePicture()
                }, 2000L)
            } else {

                //Open settings to grant permission for "Draw other apps".
                HiddenCameraUtils.openDrawOverPermissionSetting(this)
            }
        } else {

            //TODO Ask your parent activity for providing runtime permission
            Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show()
        }
        return START_NOT_STICKY
    }

    override fun onImageCapture(imageFile: File) {
        //Toast.makeText(this, "Captured image size is : " + imageFile.length(), Toast.LENGTH_SHORT).show()
        // Do something with the image...
        stopSelf()
    }

    override fun onCameraError(@CameraErrorCodes errorCode: Int) {
        when (errorCode) {
            CameraError.ERROR_CAMERA_OPEN_FAILED ->                 //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show()
            CameraError.ERROR_IMAGE_WRITE_FAILED ->                 //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show()
            CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ->                 //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show()
            CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ->                 //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this)
            CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> Toast.makeText(
                this,
                R.string.error_not_having_camera,
                Toast.LENGTH_LONG
            ).show()
        }
        stopSelf()
    }
}