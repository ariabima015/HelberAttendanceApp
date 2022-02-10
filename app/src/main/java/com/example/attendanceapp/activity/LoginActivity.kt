package com.example.attendanceapp.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.attendanceapp.R
import com.example.attendanceapp.model.BasicResponse
import com.example.attendanceapp.model.LoginResponse
import com.example.attendanceapp.network.RetrofitInstance
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail : TextInputEditText
    private lateinit var etPassword : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)

    }

    fun loginOnClick(view: android.view.View) {
        val api = RetrofitInstance().createApi()

        lifecycleScope.launchWhenCreated {
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
                    val intent = Intent(applicationContext, AttendanceActivity::class.java)
                    startActivity(intent)
                } else {
                    var jsonObject = JSONObject(response.errorBody()?.string());
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
        }
    }


}