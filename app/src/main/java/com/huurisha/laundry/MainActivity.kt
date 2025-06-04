package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
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
import com.huurisha.laundry.cabang.Data_cabang
import com.huurisha.laundry.layanan.dataLayanan
import com.huurisha.laundry.pegawai.dataPegawai
import com.huurisha.laundry.pegawai.tambah_pegawai
import com.huurisha.laundry.pelanggan.dataPelanggan
import com.huurisha.laundry.pelanggan.tambahPelanggan
import com.huurisha.laundry.tambahan.datatambahan
import com.huurisha.laundry.transaksi.Transaksi

import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var pelanggan1: ImageButton
    lateinit var pegawai1: ImageView
    lateinit var layanan:CardView
    lateinit var tambahan:CardView
    lateinit var cabang:ImageButton
    lateinit var transaksi:ImageView
    lateinit var laporan:ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        init()
        tekan()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars()) // Sembunyikan status bar & navigation bar
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

// Menghindari perubahan padding/margin pada button atau card
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime()) // Menyesuaikan keyboard
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Status & nav bar

            v.setPadding(0, 0, 0, imeInsets.bottom) // Pastikan padding tidak berubah
            insets
        }
    }

        fun init() {
        pelanggan1 = findViewById(R.id.pelanggan1)
        pegawai1 = findViewById(R.id.pegawai)
        layanan = findViewById(R.id.layanan)
        tambahan = findViewById(R.id.tambahan)
        cabang = findViewById(R.id.cabang2)
        transaksi = findViewById(R.id.tras)
            laporan = findViewById(R.id.imagebuttonlaporan)

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

        layanan.setOnClickListener{
            val intent = Intent(this,dataLayanan::class.java)
            startActivity(intent)
        }

        tambahan.setOnClickListener {
            val intent =  Intent(this, datatambahan::class.java)
            startActivity(intent)
        }

        cabang.setOnClickListener {
            val intent =  Intent(this, Data_cabang::class.java)
            startActivity(intent)
        }

        transaksi.setOnClickListener {
            val intent = Intent(this, Transaksi::class.java)
            startActivity(intent)
        }
        laporan.setOnClickListener{
            val intent = Intent(this,data_laporan::class.java)
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
            hour in 5..11 -> this.getString(R.string.pagi)
            hour in 12..14 -> this.getString(R.string.siang)
            hour in 15..17 -> this.getString(R.string.sore)
            else -> this.getString(R.string.malam)
        }
        helloTextView.text = greeting
    }
}

