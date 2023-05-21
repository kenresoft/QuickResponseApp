package com.kixfobby.security.quickresponse.ui

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import java.io.*


class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    var longi: Double? = null
    var lati: Double? = null
    private var mMap: GoogleMap? = null
    lateinit var imageFile: File
    lateinit var share: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        val bundle = intent.extras
        if (bundle != null) {
            longi = bundle.getDouble("long")
            lati = bundle.getDouble("lat")
            BaseActivity().toast(this, lati.toString() + " " + longi.toString())
        } else {
            BaseActivity().toast(this, "Location not available!")
        }

        val directory = File(shotFolder)
        if (!directory.exists()) {
            directory.mkdir()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val lng = LatLng(lati!!, longi!!)
        mMap!!.addMarker(MarkerOptions().position(lng).title("Current Location"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(lng))
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        var bt_switch_1 = findViewById<MaterialButton>(R.id.bt_switch_1)
        var bt_switch_2 = findViewById<MaterialButton>(R.id.bt_switch_2)
        var bt_switch_3 = findViewById<MaterialButton>(R.id.bt_switch_3)
        var capture = findViewById<Button>(R.id.bt_capture)
        share = findViewById<Button>(R.id.bt_share)

        bt_switch_1.setOnClickListener {
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap!!.setMyLocationEnabled(false)
        }
        bt_switch_2.setOnClickListener {
            mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
            mMap!!.setMyLocationEnabled(false)
        }
        bt_switch_3.setOnClickListener {
            mMap!!.setMyLocationEnabled(true)
        }

        capture.setOnClickListener(View.OnClickListener {
            captureScreen()
        })
    }

    fun captureScreen() {
        val callback = SnapshotReadyCallback { snapshot ->
            var bitmap = snapshot
            var fout: OutputStream?
            var filePath = shotFolder + "qr_map_shot_" + System.currentTimeMillis().toString() + ".jpg"
            imageFile = File(filePath)
            try {
                fout = FileOutputStream(imageFile)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fout)
                fout.flush()
                fout.close()
            } catch (e: FileNotFoundException) {
                Log.d("ImageCapture", "FileNotFoundException")
                e.message?.let { Log.d("ImageCapture", it) }
            } catch (e: IOException) {
                Log.d("ImageCapture", "IOException")
                e.message?.let { Log.d("ImageCapture", it) }
            }
            Toast.makeText(applicationContext, "Screenshot saved!", Toast.LENGTH_SHORT).show()

            share.visibility = View.VISIBLE
            share.setOnClickListener(View.OnClickListener {
                openShareImageDialog(imageFile)
            })

        }
        mMap!!.snapshot(callback)
    }

    fun openShareImageDialog(filePath: File) {
        val values = ContentValues(2)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATA, filePath.toString())
        val contentUriFile = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, contentUriFile)
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    /*private fun openScreenshot(imageFile: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.provider", imageFile)
        intent.setDataAndType(uri, "image/.")
        startActivity(intent)
    }*/

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onResume() {
        super.onResume()
        share = findViewById<Button>(R.id.bt_share)
        share.visibility = View.GONE
    }

    companion object {
        val shotFolder = BaseActivity.storageLocation + "screenshots/"
    }
}