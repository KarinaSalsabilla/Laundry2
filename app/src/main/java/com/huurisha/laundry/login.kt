package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class login : AppCompatActivity() {

    private lateinit var etNomorHP: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etNomorHP = findViewById(R.id.phone)
        etPassword = findViewById(R.id.inputpw)
        btnLogin = findViewById(R.id.simpanlogin)

        btnLogin.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val nomorHP = etNomorHP.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validasi input
        if (nomorHP.isEmpty()) {
            etNomorHP.error = "Nomor HP tidak boleh kosong"
            etNomorHP.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password tidak boleh kosong"
            etPassword.requestFocus()
            return
        }

        // Cek dengan data lokal
        val validNomorHP = "087833524624"
        val validPassword = "admin"

        if (nomorHP == validNomorHP && password == validPassword) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Login berhasil!", Toast.LENGTH_LONG).show()
            // Tidak mengirimkan data ke mana pun
        } else {
            Toast.makeText(this, "Nomor HP atau password salah", Toast.LENGTH_SHORT).show()
        }
    }
}
