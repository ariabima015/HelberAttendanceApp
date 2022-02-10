package com.example.attendanceapp.activity


import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.SimpleStorageHelper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.attendanceapp.InputStreamRequestBody
import com.example.attendanceapp.R
import com.example.attendanceapp.fragment.UploadFragment
import com.example.attendanceapp.network.RetrofitInstance
import com.swein.easypermissionmanager.EasyPermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AttendanceActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val easyPermissionManager = EasyPermissionManager(this)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var tvTanggal: TextView
    private lateinit var tvNama: TextView
    private lateinit var etJobdesk : EditText
    private val api = RetrofitInstance().createApi()
    private val storageHelper = SimpleStorageHelper(this)
    private var tempFile : DocumentFile? = null
    private lateinit var takePhoto : ActivityResultLauncher<Void?>
    private val currentTime: String =
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    private val imageView: ImageView by lazy {
        findViewById(R.id.imageView)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        tvNama = findViewById(R.id.tvNama)
        tvTanggal = findViewById(R.id.tvTanggal)
        etJobdesk = findViewById(R.id.etJobdesk)
        takePhoto = setupTakePhotoContract()

        setupStorageHelper()

        lifecycleScope.launchWhenCreated{
            try {
                val response = api.getUser()

                if (response.isSuccessful) {

                    val user = response.body()?.data?.get(0)?.name
                    tvNama.text = user

                } else {
                    throw Exception(response.message())
                }
            } catch (e: Exception) {
                val exception = e.message.toString()
                Toast.makeText(this@AttendanceActivity, exception, Toast.LENGTH_SHORT).show()
            }
        }

        tvTanggal.text = currentTime
        setSupportActionBar(toolbar)

        navigationView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setCheckedItem(R.id.nav_masuk)

        Glide.with(this)
            .load(R.drawable.ic_gallery_iv)
            .apply(RequestOptions().override(350, 350))
            .into(imageView)
    }


    private fun setupStorageHelper() {
        storageHelper.onFileSelected = { _, file ->
            val selectedFile = file[0]
            val targetFolder = DocumentFileCompat.fromFullPath(
                applicationContext,
                filesDir.absolutePath
            )
            if (targetFolder != null) {
                tempFile = selectedFile
                Glide.with(this)
                    .load(selectedFile.uri)
                    .apply(RequestOptions().override(350, 350))
                    .into(imageView)
            }
        }
    }

    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    private fun bitmapToFile(imageBitmap : Bitmap) : File {
        val wrapper = ContextWrapper(this)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        val stream: OutputStream = FileOutputStream(file)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG,25,stream)
        stream.flush()
        stream.close()
        return file
    }

    private fun setupTakePhotoContract() = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        if (it != null) {
            tempFile = DocumentFile.fromFile(bitmapToFile(it))
            Glide.with(this)
                .load(it)
                .apply(RequestOptions().override(350, 350))
                .into(imageView)
        }
    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }


    }

    fun presensiMasukOnClick(view: android.view.View) {


        val now = Calendar.getInstance().timeInMillis
        val time: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
            (now/1000).toString())
        val status: RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
            "COMING")
        val jobdesk :RequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(),
            etJobdesk.text.toString())

        if (tempFile == null || etJobdesk.text.toString().isEmpty()){
           Toast.makeText(this, "Mohon isi jobdesk dan bukti foto kehadiran",
               Toast.LENGTH_LONG).show()
        }else{

            val requestFile: RequestBody = InputStreamRequestBody(null, contentResolver,
                tempFile?.uri)
            val img1: MultipartBody.Part =
                MultipartBody.Part.createFormData("photo", tempFile?.name, requestFile)

            lifecycleScope.launchWhenCreated {
                try {

                    val response = api.postAttendance(time, status, jobdesk, img1)
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(applicationContext, "Presensi Tercatat",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        throw Exception(response.body()?.data?.get(0)?.message)
                    }
                } catch (e: Exception) {
                    val exception = e.message.toString()
                    Toast.makeText(this@AttendanceActivity, exception, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.getItemId()) {
            R.id.nav_masuk -> {
            }
            R.id.nav_pulang -> {
                val intent = Intent(this@AttendanceActivity, GoHomeActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                val intent = Intent(this@AttendanceActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    fun uploadOnclick(view: android.view.View) {
        val dialog = UploadFragment()

        dialog.show(supportFragmentManager, "uploadDialog")
    }

    fun cameraOnClick(view: android.view.View) {

        easyPermissionManager.requestPermission(
            "permission",
            "permission are necessary",
            "setting",
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        ){
            takePhoto.launch()
        }

    }

    fun galleryOnClick(view: android.view.View) {
        easyPermissionManager.requestPermission(
            "permission",
            "permission are necessary",
            "setting",
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        ) {
            storageHelper.openFilePicker(101)
        }

    }
}

