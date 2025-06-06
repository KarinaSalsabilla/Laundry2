package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Akun : AppCompatActivity() {

    private lateinit var namaAkun: EditText
    private lateinit var passwordAkun: EditText
    private lateinit var nohpAkun: EditText
    private lateinit var updateAkun: Button

    private lateinit var logout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_akun)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logout = findViewById(R.id.logout)

        namaAkun = findViewById(R.id.namaAkun)
        passwordAkun = findViewById(R.id.passwordAkun)
        nohpAkun = findViewById(R.id.nohpakun)
        updateAkun = findViewById(R.id.updateakun)
        logout = findViewById(R.id.logout)

// Awalnya tidak bisa disentuh
        namaAkun.isEnabled = false
        passwordAkun.isEnabled = false
        nohpAkun.isEnabled = false

// Saat klik tombol update data, aktifkan semua input
        updateAkun.setOnClickListener {
            namaAkun.isEnabled = true
            passwordAkun.isEnabled = true
            nohpAkun.isEnabled = true
        }


        // Tombol logout
        logout.setOnClickListener {
            val intent = Intent(this, login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
