package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huurisha.laundry.pegawai.dataPegawai
import com.huurisha.laundry.pegawai.tambah_pegawai
import com.huurisha.laundry.pelanggan.dataPelanggan
import com.huurisha.laundry.pelanggan.tambahPelanggan

import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var pelanggan1: ImageButton
    lateinit var pegawai1: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        init()
        tekan()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        pelanggan1 = findViewById(R.id.pelanggan1)
        pegawai1 = findViewById(R.id.pegawai)
    }

    fun tekan() {
        pelanggan1.setOnClickListener {
            val intent = Intent(this, dataPelanggan::class.java)
            startActivity(intent)
        }
        pegawai1.setOnClickListener {
            val intent = Intent(this, dataPegawai::class.java)
            startActivity(intent)
        }

        // Referensi TextView
        val helloTextView = findViewById<View>(R.id.halo) as TextView
        val dateTextView = findViewById<View>(R.id.tanggal) as TextView

        // Mengatur tanggal hari ini
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        dateTextView.text = currentDate

        // Mengatur pesan berdasarkan waktu
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour in 5..11 -> "Selamat Pagi, Karina"
            hour in 12..14 -> "Selamat Siang, Karina"
            hour in 15..17 -> "Selamat Sore, Karina"
            else -> "Selamat Malam, Karina"
        }
        helloTextView.text = greeting
    }
}

