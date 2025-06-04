package com.huurisha.laundry.tambahan

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
import java.text.NumberFormat
import java.util.Locale
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.modeldata.ModelTambahan

class Tambahan : AppCompatActivity() {
    val database =  FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etharga : EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambahan)

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
        etharga = findViewById(R.id.inputharga)
        etCabang = findViewById(R.id.inputcabang)
        btSimpan = findViewById(R.id.simpan)

    }

    fun cekValidasi(){
        val nama = etNama.text.toString()
        val alamat = etharga.text.toString()
        val cabang =  etCabang.text.toString()
        val hargaStr = etharga.text.toString()

        if (nama.isEmpty()){
            etNama.error= this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return

        }

        if (hargaStr.isEmpty()) {
            etharga.error = "Harga tidak boleh kosong"
            Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
            etharga.requestFocus()
            return
        }

        val harga = hargaStr.toDoubleOrNull()
        if (harga == null || harga <= 0) {
            etharga.error = "Harga harus lebih dari 0"
            Toast.makeText(this, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show()
            etharga.requestFocus()
            return
        }


        if (alamat.isEmpty()){
            etharga.error= this.getString(R.string.validasialamat)
            Toast.makeText(this,this.getString(R.string.validasialamat), Toast.LENGTH_SHORT).show()
            etharga.requestFocus()
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
        val Tambahanbaru = myRef.push()
        val tambahanId = Tambahanbaru.key
        val data = ModelTambahan(
            tambahanId.toString(),
            etNama.text.toString(),
            etharga.text.toString(),
            etCabang.text.toString()

        )
        Tambahanbaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this,this.getString(R.string.suksestambahan), Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener{
                Toast.makeText(this,this.getString(R.string.gagaltambahan), Toast.LENGTH_SHORT).show()
            }
    }
}