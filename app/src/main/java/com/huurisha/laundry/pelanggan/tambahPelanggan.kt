package com.huurisha.laundry.pelanggan

import android.content.res.Configuration
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataPelangganAdapter
import com.huurisha.laundry.modeldata.ModelPelanggan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class tambahPelanggan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")

    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etAlamat: AutoCompleteTextView
    lateinit var etNoHP: EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan: Button

    var rvDataPelanggan: RecyclerView? = null
    var pelangganAdapter: DataPelangganAdapter? = null
    private val listPelanggan = arrayListOf<ModelPelanggan>()

    var idPelanggan: String = ""

    // Enum untuk mengelola state button
    private enum class ButtonState {
        SIMPAN, SUNTING, PERBARUI
    }

    // Variabel untuk menyimpan state button saat ini
    private var currentButtonState = ButtonState.SIMPAN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_pelanggan)

        init()
        setupAutoCompleteAlamat()
        getData()

        btSimpan.setOnClickListener {
            cekValidasi()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init() {
        tvJudul = findViewById(R.id.tvpelanggan1)
        etNama = findViewById(R.id.inputnamapel)
        etAlamat = findViewById(R.id.inputalamatpel)
        etNoHP = findViewById(R.id.inputnohppel)
        etCabang = findViewById(R.id.inputcabangpel)
        btSimpan = findViewById(R.id.simpanpel)

        // Perbaiki ID RecyclerView sesuai XML
        val recyclerView = findViewById<RecyclerView>(R.id.rvDataPelanggan)
        if (recyclerView != null) {
            rvDataPelanggan = recyclerView
            setupRecyclerView()
            loadDataPelanggan()
        }
    }

    fun setupRecyclerView() {
        rvDataPelanggan?.let { rv ->
            pelangganAdapter = DataPelangganAdapter(listPelanggan)
            rv.adapter = pelangganAdapter
            rv.layoutManager = LinearLayoutManager(this)
        }
    }

    fun loadDataPelanggan() {
        // Hanya jalankan jika RecyclerView ada
        if (rvDataPelanggan != null) {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listPelanggan.clear()
                    for (data in snapshot.children) {
                        val pelanggan = data.getValue(ModelPelanggan::class.java)
                        pelanggan?.let { listPelanggan.add(it) }
                    }
                    pelangganAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@tambahPelanggan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun setupAutoCompleteAlamat() {
        val alamatList = listOf(
            "Perum Tohudan Indah Baru Blok C3,Tohudan Wetan Colomadu,Kab.Karangayar,Jawa Tengah",
            "Jl. Malioboro, Yogyakarta",
            "Jl. Pandanaran, Semarang",
            "Jl. Sudirman, Jakarta",
            "Jl. Diponegoro, Surabaya",
            "Jl. Braga, Bandung"
        )

        val alamatAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, alamatList)
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.inputalamatpel)
        autoCompleteTextView.setAdapter(alamatAdapter)
    }

    fun getData() {
        idPelanggan = intent.getStringExtra("idPelanggan").toString()
        val judul = intent.getStringExtra("judul")
        val nama = intent.getStringExtra("namaPelanggan")
        val alamat = intent.getStringExtra("alamatPelanggan")
        val hp = intent.getStringExtra("noHpPelanggan")
        val cabang = intent.getStringExtra("idCabang")

        tvJudul.text = judul
        etNama.setText(nama)
        etAlamat.setText(alamat)
        etNoHP.setText(hp)
        etCabang.setText(cabang)

        if (!tvJudul.text.equals(this.getString(R.string.tvpelanggan))) {
            if (judul.equals(getString(R.string.editpelanggan))) {
                mati()
                btSimpan.text = getString(R.string.sunting)
                currentButtonState = ButtonState.SUNTING
            }
        } else {
            hidup()
            etNama.requestFocus()
            btSimpan.text = getString(R.string.simpan)
            currentButtonState = ButtonState.SIMPAN
        }
    }

        fun mati() {
        etNama.isEnabled = false
        etAlamat.isEnabled = false
        etNoHP.isEnabled = false
        etCabang.isEnabled = false
    }

    fun hidup() {
        etNama.isEnabled = true
        etAlamat.isEnabled = true
        etNoHP.isEnabled = true
        etCabang.isEnabled = true
    }

    fun update() {
        val pelangganRef = database.getReference("pelanggan").child(idPelanggan)
        val data = ModelPelanggan(
            idPelanggan,
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString()
        )

        val updateData = mutableMapOf<String, Any>()
        updateData["namaPelanggan"] = data.namaPelanggan.toString()
        updateData["alamatPelanggan"] = data.alamatPelanggan.toString()
        updateData["noHpPelanggan"] = data.noHpPelanggan.toString()
        updateData["idCabang"] = data.idCabang.toString()

        pelangganRef.updateChildren(updateData).addOnSuccessListener {
            Toast.makeText(this@tambahPelanggan, getString(R.string.suksespelanggan), Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@tambahPelanggan, getString(R.string.gagalpelanggan), Toast.LENGTH_SHORT).show()
        }
    }

    // FUNGSI UTAMA YANG DIPERBAIKI
    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etAlamat.text.toString()
        val noHP = etNoHP.text.toString()
        val cabang = etCabang.text.toString()

        // Validasi Nama
        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }

        if (!nama.matches(Regex("^[a-zA-Z\\s]+$"))) {
            etNama.error = getString(R.string.validasinama)
            Toast.makeText(this, getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }

        // Validasi Alamat
        if (alamat.isEmpty()) {
            etAlamat.error = this.getString(R.string.validasialamat)
            Toast.makeText(this, this.getString(R.string.validasialamat), Toast.LENGTH_SHORT).show()
            etAlamat.requestFocus()
            return
        }

        // Validasi No HP
        if (noHP.isEmpty()) {
            etNoHP.error = this.getString(R.string.validasiHp)
            Toast.makeText(this, this.getString(R.string.validasiHp), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }

        // Validasi format nomor HP
        if (!noHP.matches(Regex("^(\\+62|62|0)[0-9]{8,13}$"))) {
            etNoHP.error = getString(R.string.validasiHp)
            Toast.makeText(this, getString(R.string.validasiHp), Toast.LENGTH_SHORT).show()
            etNoHP.requestFocus()
            return
        }

        // Validasi Cabang
        if (cabang.isEmpty()) {
            etCabang.error = this.getString(R.string.validasiCabang)
            Toast.makeText(this, this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }

        // PERBAIKAN UTAMA: Gunakan enum state untuk menentukan aksi
        when (currentButtonState) {
            ButtonState.SIMPAN -> {
                simpan()
            }
            ButtonState.SUNTING -> {
                // Aktifkan form untuk editing
                hidup()
                etNama.requestFocus()
                btSimpan.text = getString(R.string.perbarui)
                currentButtonState = ButtonState.PERBARUI

            }
            ButtonState.PERBARUI -> {
                update()
            }
        }
    }

    fun simpan() {
        val pelangganBaru = myRef.push()
        val pelangganId = pelangganBaru.key
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val data = ModelPelanggan(
            pelangganId.toString(),
            etNama.text.toString(),
            etAlamat.text.toString(),
            etNoHP.text.toString(),
            etCabang.text.toString(),
            currentTime
        )

        pelangganBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.suksespelanggan), Toast.LENGTH_SHORT).show()

            // Logika baru untuk orientasi
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                clearForm() // Opsional: bersihkan form sebelum finish
                finish() // Tutup aktivitas jika dalam mode potret
            } else {
                // Dalam mode lanskap, bersihkan form dan muat ulang data di RecyclerView (jika ada)
                clearForm()
                if (rvDataPelanggan != null) {
                    loadDataPelanggan()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, this.getString(R.string.gagalpelanggan), Toast.LENGTH_SHORT).show()
        }
    }

    fun clearForm() {
        etNama.setText("")
        etAlamat.setText("")
        etNoHP.setText("")
        etCabang.setText("")
        etNama.requestFocus()
        // Reset button ke state awal
        btSimpan.text = getString(R.string.simpan)
        currentButtonState = ButtonState.SIMPAN
    }
}