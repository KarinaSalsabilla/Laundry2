package com.huurisha.laundry.pegawai

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
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelPelanggan

class tambah_pegawai : AppCompatActivity() {
        val database =  FirebaseDatabase.getInstance()
        val myRef = database.getReference("pegawai")
        lateinit var tvJudul: TextView
        lateinit var etNama: EditText
        lateinit var etAlamat : EditText
        lateinit var etNoHP : EditText
        lateinit var terdaftar : EditText
        lateinit var etCabang: EditText
        lateinit var btSimpan : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)
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
        tvJudul = findViewById(R.id.tvpegawai)
        etNama = findViewById(R.id.inputnamapegawai)
        etAlamat = findViewById(R.id.inputalamatpegawai)
        etNoHP = findViewById(R.id.inputnohppegawai)
        terdaftar = findViewById(R.id.edt_pegawai)
        etCabang = findViewById(R.id.edtcabangpegawai)
        btSimpan = findViewById(R.id.simpanpegawai)

    }

    fun cekValidasi(){
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHP = etNoHP.text.toString()
        val tdf = terdaftar.text.toString()
        val cabang =  etCabang.text.toString()

        if (nama.isEmpty()){
            etNama.error= this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return

        }

        if (alamat.isEmpty()){
            etAlamat.error= this.getString(R.string.validasialamat)
            Toast.makeText(this,this.getString(R.string.validasialamat), Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }

        if (noHP.isEmpty()){
            etNoHP.error= this.getString(R.string.validasiHp)
            Toast.makeText(this,this.getString(R.string.validasiHp), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }

        if (tdf.isEmpty()){
            terdaftar.error= this.getString(R.string.validasidaftar)
            Toast.makeText(this,this.getString(R.string.validasidaftar),Toast.LENGTH_SHORT).show()
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
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key
        val data = ModelPegawai(
            pegawaiId.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            terdaftar.text.toString(),
            etCabang.text.toString()
        )
        pegawaiBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this,this.getString(R.string.suksespelanggan), Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener{
                Toast.makeText(this,this.getString(R.string.gagalpelanggan), Toast.LENGTH_SHORT).show()
            }
    }
}
