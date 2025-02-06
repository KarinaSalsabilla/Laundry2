package com.huurisha.laundry.pelanggan

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
import com.huurisha.laundry.modeldata.ModelPelanggan

class tambahPelanggan : AppCompatActivity() {
    val database =  FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    lateinit var tvJudul:TextView
    lateinit var etNama:EditText
    lateinit var etAlamat :EditText
    lateinit var etNoHP : EditText
    lateinit var etCabang:EditText
    lateinit var btSimpan : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pelanggan)

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
        tvJudul = findViewById(R.id.tvpelanggan1)
        etNama = findViewById(R.id.inputnamapel)
        etAlamat = findViewById(R.id.inputalamatpel)
        etNoHP = findViewById(R.id.inputnohppel)
        etCabang = findViewById(R.id.inputcabangpel)
        btSimpan = findViewById(R.id.simpanpel)

    }

    fun cekValidasi(){
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHP = etNoHP.text.toString()
        val cabang =  etCabang.text.toString()

        if (nama.isEmpty()){
            etNama.error= this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama),Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return

        }

        if (alamat.isEmpty()){
            etAlamat.error= this.getString(R.string.validasialamat)
            Toast.makeText(this,this.getString(R.string.validasialamat),Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }

        if (noHP.isEmpty()){
            etNoHP.error= this.getString(R.string.validasiHp)
            Toast.makeText(this,this.getString(R.string.validasiHp),Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }
        if (cabang.isEmpty()){
            etCabang.error= this.getString(R.string.validasiCabang)
            Toast.makeText(this,this.getString(R.string.validasiCabang),Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }
        simpan()
    }

    fun simpan(){
        val pelangganBaru = myRef.push()
        val pelangganId = pelangganBaru.key
        val data = ModelPelanggan(
            pelangganId.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString()
        )
        pelangganBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this,this.getString(R.string.suksespelanggan),Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener{
                Toast.makeText(this,this.getString(R.string.gagalpelanggan),Toast.LENGTH_SHORT).show()
            }
    }
}