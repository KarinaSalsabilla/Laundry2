package com.huurisha.laundry.pegawai

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.huurisha.laundry.R

class dataPegawai : AppCompatActivity() {
    lateinit var tambahpega : FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)
        init()
        tekan()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init(){
        tambahpega = findViewById(R.id.fabDATA_PEGAWAI_Tambah)
    }
    fun tekan(){

        tambahpega.setOnClickListener{
            val intent = Intent(this, tambah_pegawai::class.java)
            startActivity(intent)
        }
    }
}