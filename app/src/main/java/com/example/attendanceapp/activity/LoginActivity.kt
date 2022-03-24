package com.example.attendanceapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.attendanceapp.R
import com.example.attendanceapp.network.RetrofitInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONException
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail : TextInputEditText
    private lateinit var etPassword : TextInputEditText
    private lateinit var pbLogin : ProgressBar
    private var WRITE_EXTERNAL_STORAGE_PERMISSION_CODE: Int = 1
    private var READ_EXTERNAL_STORAGE_PERMISSION_CODE: Int = 2
    private var CAMERA_PERMISSION_CODE: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        pbLogin = findViewById(R.id.pbLogin)


        checkPermission()

    }

    fun loginOnClick(view: android.view.View) {
        val api = RetrofitInstance().createApi()

        lifecycleScope.launchWhenCreated {
            pbLogin.visibility = View.VISIBLE
            try {
                val body = hashMapOf(
                    "email" to etEmail.text.toString(),
                    "password" to etPassword.text.toString()
                )
                val response = api.login(body)
                val pref =
                    getSharedPreferences(RetrofitInstance().PREF_KEY, Context.MODE_PRIVATE).edit()
                pref.putString("token", response.body()?.data?.get(0)?.token)
                pref.apply()
                if (response.isSuccessful && response.body() != null) {
                    openCamera()
                } else {
                    var jsonObject = JSONObject(response.errorBody()?.string())
                    if(!jsonObject.getBoolean("success")) {
                        var jsonArray = jsonObject.getJSONArray("data")
                        throw Exception(JSONObject(jsonArray.get(0).toString()).getString("message"))
                    }
                    else
                        throw Exception("Error Serialize Json")
                }
            } catch (e: Exception) {
                val exception = e.message.toString()
                Toast.makeText(this@LoginActivity, exception, Toast.LENGTH_SHORT).show()
            }
            pbLogin.visibility = View.GONE
        }
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_DENIED) {
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
                    )
                }
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_EXTERNAL_STORAGE_PERMISSION_CODE
                    )
                }
                checkSelfPermission(Manifest.permission.CAMERA) -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            READ_EXTERNAL_STORAGE_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            CAMERA_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun openCamera() {
        val qrScan = IntentIntegrator(this)
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        qrScan.setPrompt("Scan a QR Code")
        qrScan.setOrientationLocked(false)
        qrScan.setBeepEnabled(false)
        qrScan.setBarcodeImageEnabled(true)
        qrScan.setTorchEnabled(false)
        //initiating the qr code scan
        qrScan.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        val hasil = "66e7888ff47adafa2b512fdf3002c78dc91d2d756a1194c6163a3972513c7a64"
        val contents = result.contents
        if (result != null)
        {
            //if qrcode has nothing in it
            if (contents == null){
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
            } else if(contents != hasil){
                Toast.makeText(this, "Wrong QR Code", Toast.LENGTH_LONG).show()
            }else{
                //if qr contains data
                try {
                    val intent = Intent(this, AttendanceActivity::class.java)
                    startActivity(intent)
                }
                catch (e: JSONException) {
                    e.printStackTrace()
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}