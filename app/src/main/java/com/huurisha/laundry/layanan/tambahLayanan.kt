package com.huurisha.laundry.layanan

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelLayanan
import com.huurisha.laundry.modeldata.ModelPegawai

class tambahLayanan : AppCompatActivity() {
    val database =  FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga : EditText
    lateinit var etCabang : EditText
    lateinit var btSimpan : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_layanan)

        init()

        btSimpan.setOnClickListener{
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        tvJudul = findViewById(R.id.tvlayanan)
        etNama = findViewById(R.id.inputnamalayanan)
        etHarga = findViewById(R.id.inputharga)
        etCabang = findViewById(R.id.inputcabang)
        btSimpan = findViewById(R.id.simpan)

    }

    fun cekValidasi(){
        val nama = etNama.text.toString()
        val harga = etHarga.text.toString()
        val cabang =  etCabang.text.toString()

        if (nama.isEmpty()){
            etNama.error= this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return

        }

        if (harga.isEmpty()){
            etHarga.error= this.getString(R.string.validasiHarga)
            Toast.makeText(this,this.getString(R.string.validasiHarga), Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }

        if (cabang.isEmpty()){
            etCabang.error= this.getString(R.string.validasiCabang)
            Toast.makeText(this,this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }
        simpan()
    }

    fun simpan(){
        val layananBaru = myRef.push()
        val layananId = layananBaru.key
        val data = ModelLayanan(
            layananId.toString(),
            etNama.text.toString(),
            etHarga.text.toString(),
            etCabang.text.toString()
        )
        layananBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this,this.getString(R.string.sukseslayanan), Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener{
                Toast.makeText(this,this.getString(R.string.gagalayanan), Toast.LENGTH_SHORT).show()
            }
    }
}