package com.huurisha.laundry.pegawai

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class tambah_pegawai : AppCompatActivity() {
        val database =  FirebaseDatabase.getInstance()
        val myRef = database.getReference("pegawai")
        lateinit var tvJudul: TextView
        lateinit var etNama: EditText
        lateinit var etAlamat : AutoCompleteTextView
        lateinit var etNoHP : EditText
        lateinit var etCabang: EditText
        lateinit var btSimpan : Button

        var idPegawai:String= ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pegawai)
        init()
        setupAutoCompleteAlamat()
        getData()

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
        etCabang = findViewById(R.id.edtcabangpegawai)
        btSimpan = findViewById(R.id.simpanpegawai)

    }

    fun setupAutoCompleteAlamat() {
        val alamatList = listOf("Perum Tohudan Indah Baru Blok C3,Tohudan Wetan Colomadu,Kab.Karangayar,Jawa Tengah",
            "Jl. Malioboro, Yogyakarta",
            "Jl. Pandanaran, Semarang",
            "Jl. Sudirman, Jakarta",
            "Jl. Diponegoro, Surabaya",
            "Jl. Braga, Bandung")

        val alamatAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, alamatList)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.inputalamatpegawai)
        autoCompleteTextView.setAdapter(alamatAdapter)
    }

    fun getData(){
        idPegawai = intent.getStringExtra("idPegawai").toString()
        val judul = intent.getStringExtra("judul")
        val nama = intent.getStringExtra("namaPegawai")
        val alamat = intent.getStringExtra("alamatPegawai")
        val hp = intent.getStringExtra("noHpPegawai")
        val cabang = intent.getStringExtra("idCabangPegawai")

        tvJudul.text = judul
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(hp)
        etCabang.setText(cabang)
        if (!tvJudul.text.equals(this.getString(R.string.tvpegawai))){
            if (judul.equals("Edit Pegawai")){
                mati()
                btSimpan.text = "Sunting"
            }
        }else{
            hidup()
            etNama.requestFocus()
            btSimpan.text="Simpan"
        }
    }

    fun mati(){
        etNama.isEnabled=false
        etAlamat.isEnabled=false
        etNoHP.isEnabled=false
        etCabang.isEnabled=false
    }

    fun hidup(){
        etNama.isEnabled=true
        etAlamat.isEnabled=true
        etNoHP.isEnabled=true
        etCabang.isEnabled=true
    }

    fun update(){
        val pegawaiRef =  database.getReference("pegawai").child(idPegawai)
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val data = ModelPegawai(
            idPegawai,
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString(),
            currentTime

        )

        val updateData = mutableMapOf<String,Any>()
        updateData["namaPegawai"] = data.namaPegawai.toString()
        updateData["alamatPegawai"] = data.alamatPegawai.toString()
        updateData["noHpPegawai"] = data.noHpPegawai.toString()
        updateData["idCabangPegawai"] = data.idCabangPegawai.toString()
        pegawaiRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this@tambah_pegawai,"Data Pegawai Berhasil Diperbarui",Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@tambah_pegawai,"Data Pegawai Gagal Diperbarui",Toast.LENGTH_SHORT).show()
        }
    }

    fun cekValidasi(){
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHP = etNoHP.text.toString()
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

        if (cabang.isEmpty()){
            etCabang.error= this.getString(R.string.validasiCabang)
            Toast.makeText(this,this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }
       if (btSimpan.text.equals("Simpan")){
           simpan()
       }else if (btSimpan.text.equals("Sunting")){
           hidup()
           etNama.requestFocus()
           btSimpan.text="Perbarui"
       }else if (btSimpan.text.equals("Perbarui")){
           update()
       }
    }

    fun simpan(){
        val pegawaiBaru = myRef.push()
        val pegawaiId = pegawaiBaru.key
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val data = ModelPegawai(
            pegawaiId.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString(),
            currentTime
        )
        pegawaiBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this,this.getString(R.string.suksespegawai), Toast.LENGTH_SHORT).show()
            finish()
        }
            .addOnFailureListener{
                Toast.makeText(this,this.getString(R.string.gagalpegawai), Toast.LENGTH_SHORT).show()
            }
    }
}
