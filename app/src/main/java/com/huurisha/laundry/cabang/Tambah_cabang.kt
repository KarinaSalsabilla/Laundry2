package com.huurisha.laundry.cabang

import android.app.TimePickerDialog
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
import com.huurisha.laundry.adapter.DataCabangAdapter
import com.huurisha.laundry.modeldata.ModelCabang
import java.util.Calendar

class Tambah_cabang : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("cabang")

    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etalamat: AutoCompleteTextView
    lateinit var etnohp: EditText
    lateinit var etjam: EditText
    lateinit var btSimpan: Button

    // Tambahkan deklarasi variabel untuk RecyclerView
    var rvDataCabang: RecyclerView? = null
    var cabangAdapter: DataCabangAdapter? = null
    private val listCabang = arrayListOf<ModelCabang>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_cabang)

        init()
        setupAutoCompleteAlamat()

        etjam.setOnClickListener {
            pilihJamOperasional()
        }

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
        etNama = findViewById(R.id.inputcabang)
        etalamat = findViewById(R.id.inputalamat)
        etnohp = findViewById(R.id.inputnohp)
        etjam = findViewById(R.id.jamopera)
        btSimpan = findViewById(R.id.simpanpel)

        // Cek apakah RecyclerView ada (hanya di landscape)
        val recyclerView = findViewById<RecyclerView>(R.id.rvDataCabang)
        if (recyclerView != null) {
            rvDataCabang = recyclerView
            setupRecyclerView()
            loadDataCabang()
        }
    }

    fun setupRecyclerView() {
        rvDataCabang?.let { rv ->
            cabangAdapter = DataCabangAdapter(listCabang)
            rv.adapter = cabangAdapter
            rv.layoutManager = LinearLayoutManager(this)
        }
    }

    fun loadDataCabang() {
        // Hanya jalankan jika RecyclerView ada
        if (rvDataCabang != null) {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listCabang.clear()
                    for (data in snapshot.children) {
                        val cabang = data.getValue(ModelCabang::class.java)
                        cabang?.let { listCabang.add(it) }
                    }
                    cabangAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tambah_cabang, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun pilihJamOperasional() {
        val calendar = Calendar.getInstance()

        // Pilih Jam Buka
        TimePickerDialog(this, { _, hourOfDayStart, minuteStart ->
            val jamBuka = String.format("%02d:%02d", hourOfDayStart, minuteStart)

            // Setelah jam buka dipilih, tampilkan dialog lagi untuk jam tutup
            TimePickerDialog(this, { _, hourOfDayEnd, minuteEnd ->
                val jamTutup = String.format("%02d:%02d", hourOfDayEnd, minuteEnd)
                etjam.setText("$jamBuka - $jamTutup")
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
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
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.inputalamat)
        autoCompleteTextView.setAdapter(alamatAdapter)
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val alamat = etalamat.text.toString()
        val nohp = etnohp.text.toString()
        val jam = etjam.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }

        if (alamat.isEmpty()) {
            etalamat.error = this.getString(R.string.validasiHarga)
            Toast.makeText(this, this.getString(R.string.validasiHarga), Toast.LENGTH_SHORT).show()
            etalamat.requestFocus()
            return
        }

        if (nohp.isEmpty()) {
            etnohp.error = this.getString(R.string.validasiCabang)
            Toast.makeText(this, this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etnohp.requestFocus()
            return
        }

        if (jam.isEmpty()) {
            etjam.error = this.getString(R.string.validasiCabang)
            Toast.makeText(this, this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etjam.requestFocus()
            return
        }
        simpan()
    }

    fun simpan() {
        val cabangBaru = myRef.push()
        val cabangId = cabangBaru.key
        val data = ModelCabang(
            cabangId.toString(),
            etNama.text.toString(),
            etalamat.text.toString(),
            etnohp.text.toString(),
            etjam.text.toString()
        )
        cabangBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.suksescabang), Toast.LENGTH_SHORT).show()

            // Logika baru untuk orientasi
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                clearForm() // Opsional: bersihkan form sebelum finish
                finish() // Tutup aktivitas jika dalam mode potret
            } else {
                // Dalam mode lanskap, bersihkan form dan muat ulang data di RecyclerView (jika ada)
                clearForm()
                if (rvDataCabang != null) {
                    loadDataCabang()
                }
            }
        }
//            Toast.makeText(this, this.getString(R.string.suksescabang), Toast.LENGTH_SHORT).show()
//            // Hanya refresh jika RecyclerView ada (landscape)
//            if (rvDataCabang != null) {
//                loadDataCabang()
//            }
//            clearForm()
//        }
            .addOnFailureListener {
                Toast.makeText(this, this.getString(R.string.gagalcabang), Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        etNama.setText("")
        etalamat.setText("")
        etnohp.setText("")
        etjam.setText("")
    }
}