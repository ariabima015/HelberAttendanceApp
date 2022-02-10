package com.example.attendanceapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.attendanceapp.R
import com.example.attendanceapp.network.RetrofitInstance
import com.google.android.material.textfield.TextInputEditText


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
                Log.e("error", response.body().toString())
                if (response.isSuccessful && response.body() != null) {
                    val intent = Intent(applicationContext, AttendanceActivity::class.java)
                    startActivity(intent)
                } else {

                    Log.e("error", response.errorBody().toString())
                    throw Exception(response.body()?.data?.get(0)?.message.toString())
                }
            } catch (e: Exception) {
                val exception = e.message.toString()
                Toast.makeText(this@LoginActivity, exception, Toast.LENGTH_SHORT).show()
            }
        }
    }
}