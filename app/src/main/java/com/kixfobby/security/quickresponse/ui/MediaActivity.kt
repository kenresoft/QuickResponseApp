package com.kixfobby.security.quickresponse.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
//import com.afollestad.materialcamera.MaterialCamera
import com.bumptech.glide.Glide
import com.kixfobby.security.quickresponse.BaseActivity
import com.kixfobby.security.quickresponse.R
import com.kixfobby.security.quickresponse.ui.MediaActivity
import com.kixfobby.security.quickresponse.util.Commons
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class MediaActivity : BaseActivity() {
    var gallery: GridView? = null
    private var help_note: String? = null
    private val mediaFiles: MutableList<File> = ArrayList()
    private var adapter: MediaFileAdapter? = null
    private var saveDir: File? = null
    private val file_path_text: TextView? = null

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun save(mBitmap: Bitmap?) {
        val filename: String
        val date = Date(0)
        val sdf = SimpleDateFormat("yyyyMMddHHmmss")
        filename = sdf.format(date)
        try {
            val path = Environment.getExternalStorageDirectory().toString()
            var fOut: OutputStream? = null
            val file = File(path, "/DCIM/Signatures/$filename.jpg")
            fOut = FileOutputStream(file)
            mBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, fOut)
            fOut.flush()
            fOut.close()
            MediaStore.Images.Media.insertImage(
                contentResolver, file.absolutePath, file.name, file.name
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_main)
        //ButterKnife.bind(this);
        gallery = findViewById(R.id.gallery)
        supportActionBar!!.setTitle(R.string.media_files)
        help_note = String.format(getString(R.string.media_files_are_stored_under), Commons.MEDIA_DIR)
        adapter = MediaFileAdapter(this, mediaFiles)
        if (gallery != null) {
            gallery!!.adapter = adapter
            gallery!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val file = adapter!!.getItem(position)
                //MediaActivity.this.playOrViewMedia(file);
            }
        }
    }

    private fun startVideoRecordActivity() {
        //val intent = Intent(this, CameraActivity::class.java)
        showProgressDialog()
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val file = File( /*Commons.MEDIA_DIR*/Environment.getExternalStorageDirectory()
            .toString() + "/Kixfobby/QuickResponse/Media"
        )
        if (file.isDirectory) {
            mediaFiles.clear()
            val files = file.listFiles()
            Arrays.sort(files) { f1, f2 ->
                if (f1.lastModified() - f2.lastModified() == 0L) {
                    0
                } else {
                    if (f1.lastModified() - f2.lastModified() > 0) -1 else 1
                }
            }
            mediaFiles.addAll(Arrays.asList(*files))
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun playOrViewMedia(file: File) {
        val intent = Intent(this@MediaActivity, VideoPlayActivity::class.java)
        var uriForFile =
            FileProvider.getUriForFile(this@MediaActivity, applicationContext.packageName + ".provider2", file)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uriForFile = Uri.fromFile(file)
        }
        intent.setDataAndType(uriForFile, if (isVideo(file)) "video/mp4" else "image/jpg")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        if (intent.type == "video/mp4") {
            showProgressDialog()
        }
        startActivity(intent)
    }

    /*fun startRecordVideo() {
        //file_path_text.setText("");
        saveDir = File(Environment.getExternalStorageDirectory(), "Kixf")
        saveDir!!.mkdirs()
        val materialCamera = MaterialCamera(this@MediaActivity)
            .saveDir(saveDir)
            .showPortraitWarning(true)
            .allowRetry(true)
            .defaultToFrontFacing(true)
            .allowRetry(true)
            .autoSubmit(false)
            .labelConfirm(R.string.mcam_use_video)
        materialCamera.start(CAMERA_RQ, false)
    }*/

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_RQ -> try {
                    var filePath = intent!!.getStringExtra("videoUrl")
                    Log.e("lzf_video", filePath!!)
                    if (filePath != null && filePath != "") {
                        if (filePath.startsWith("file://")) {
                            filePath = intent.getStringExtra("videoUrl")!!.substring(7, filePath.length)
                            file_path_text!!.text = "abc$filePath"
                        }
                    }
                } catch (ex: Exception) {
                }
                Image_Capture_Code -> {
                    val bp = intent!!.extras!!["data"] as Bitmap?
                    //savebitmap(bp);
                    try {
                        save(bp)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CHECK_PERMISSION && grantResults.size == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
            //startRecordVideo()
        }
    }

    @Contract("null -> false")
    private fun isVideo(file: File?): Boolean {
        return file != null && file.name.endsWith(".mp4")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        menu.add(Menu.NONE, 0, Menu.NONE, "open video").setIcon(
            ContextCompat.getDrawable(baseContext, R.drawable.baseline_videocam_24)
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.open_camera).setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_perm_media_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.help)).setIcon(
            ContextCompat.getDrawable(
                baseContext, R.drawable.baseline_help_24
            )
        ).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                //startVideoRecordActivity();

                /* Intent cInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cInt, Image_Capture_Code);*/if (ContextCompat.checkSelfPermission(
                        this@MediaActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this@MediaActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this@MediaActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        this@MediaActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //startRecordVideo();
                    //startActivity(Intent(baseContext, CameraActivity::class.java))
                } else {
                    ActivityCompat.requestPermissions(
                        this@MediaActivity, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO
                        ), CHECK_PERMISSION
                    )
                }
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            1 -> {
                val cInt2 = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cInt2, Image_Capture_Code)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                return true
            }
            2 -> {
                val alert = AlertDialog.Builder(this, R.style.CustomDialogTheme)
                alert.setTitle(getString(R.string.help) + " !")
                alert.setMessage(help_note)
                alert.setCancelable(true)
                alert.create().show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private inner class MediaFileAdapter internal constructor(
        private val context: Context,
        private val files: List<File>
    ) : BaseAdapter() {

        private val mInflator: LayoutInflater

        init {
            this.mInflator = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return files.size
        }

        override fun getItem(position: Int): File {
            return files[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view: View?
            val vh: ListRowHolder
            if (convertView == null) {
                view = this.mInflator.inflate(R.layout.item_media, parent, false)
                vh = ListRowHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ListRowHolder
            }

            vh.file = files[position]
            Glide.with(context).load(vh.file).into(vh.imageView)
            if (isVideo(vh.file)) {
                vh.indicator.visibility = View.VISIBLE
            } else {
                vh.indicator.visibility = View.GONE
            }

            return convertView
        }

        private inner class ListRowHolder(row: View?) {
            public val imageView: ImageView
            public val indicator: View
            public lateinit var file: File

            init {
                this.imageView = row?.findViewById(R.id.item_image)!!
                this.indicator = row.findViewById<View>(R.id.item_indicator)
            }
        }
    }

    companion object {
        private const val Image_Capture_Code = 1
        private const val CAMERA_RQ = 8099
        private const val CHECK_PERMISSION = 8001
    }
}