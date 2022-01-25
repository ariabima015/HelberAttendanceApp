package com.example.attendanceapp.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.attendanceapp.R
import com.example.attendanceapp.fragment.UploadFragment
import com.google.android.material.navigation.NavigationView
import com.swein.easypermissionmanager.EasyPermissionManager
import java.io.File


class GoHomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val easyPermissionManager = EasyPermissionManager(this)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar


    private val imageView: ImageView by lazy {
        findViewById(R.id.imageView)
    }

    private val selectPictureLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            imageView.setImageURI(it)
            Glide.with(this)
                .load(it)
                .apply(RequestOptions().override(350, 350))
                .into(imageView)
            Log.d("image created", "$tempImageUri")
        }

    private var tempImageUri: Uri? = null
    private var tempImageFilePath = "" //
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageView.setImageURI(tempImageUri)
                Glide.with(this)
                    .load(tempImageUri)
                    .apply(RequestOptions().override(350, 350))
                    .into(imageView)
                Log.d("image created", "$tempImageUri")

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_home)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)


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
        navigationView.setCheckedItem(R.id.nav_pulang)

        Glide.with(this)
            .load(R.drawable.ic_gallery_iv)
            .apply(RequestOptions().override(350, 350))
            .into(imageView)
    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }


    }

    fun presensiPulangOnClick(view: android.view.View) {
        val text = "Presensi Tercatat"
        val duration = Toast.LENGTH_SHORT

        val toast = Toast.makeText(applicationContext, text, duration)
        toast.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_masuk -> {
                finish()
            }
            R.id.nav_pulang -> {
            }
            R.id.nav_logout -> {
                val intent = Intent(this@GoHomeActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun createImageFile() : File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("temp_image", ".jpg", storageDir)
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
            tempImageUri = FileProvider.getUriForFile(this,
                "com.example.attendanceapp.provider", createImageFile().also {
                    tempImageFilePath = it.absolutePath
                })

            cameraLauncher.launch(tempImageUri)
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
            selectPictureLauncher.launch("image/*")
        }
        
    }




}