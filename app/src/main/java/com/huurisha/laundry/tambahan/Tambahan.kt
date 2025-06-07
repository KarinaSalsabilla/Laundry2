package com.huurisha.laundry.tambahan

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataTambahanAdapter
import com.huurisha.laundry.modeldata.ModelTambahan

class Tambahan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")

    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga: EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan: Button

    var rvDataTambahan: RecyclerView? = null
    var tambahanAdapter: DataTambahanAdapter? = null
    private val listTambahan = arrayListOf<ModelTambahan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambahan)

        init()
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
        // Initialize form views - semua ID ini ada di kedua layout
        tvJudul = findViewById(R.id.tvlayanan)
        etNama = findViewById(R.id.inputnamalayanan)
        etHarga = findViewById(R.id.inputharga)
        etCabang = findViewById(R.id.inputcabang)
        btSimpan = findViewById(R.id.simpan)

        // RecyclerView hanya ada di landscape mode
        rvDataTambahan = findViewById(R.id.rvDataTambahan)

        // Setup RecyclerView jika ada (landscape mode)
        rvDataTambahan?.let { rv ->
            setupRecyclerView()
            loadDataTambahan()
        }
    }

    fun setupRecyclerView() {
        rvDataTambahan?.let { rv ->
            tambahanAdapter = DataTambahanAdapter(this,listTambahan)
            rv.adapter = tambahanAdapter
            rv.layoutManager = LinearLayoutManager(this)
        }
    }

    fun loadDataTambahan() {
        // Hanya jalankan jika RecyclerView ada (landscape mode)
        rvDataTambahan?.let {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listTambahan.clear()
                    for (data in snapshot.children) {
                        val tambahan = data.getValue(ModelTambahan::class.java)
                        tambahan?.let { listTambahan.add(it) }
                    }
                    tambahanAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@Tambahan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun cekValidasi() {
        val nama = etNama.text.toString().trim()
        val hargaStr = etHarga.text.toString().trim()
        val cabang = etCabang.text.toString().trim()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }

        if (hargaStr.isEmpty()) {
            etHarga.error = "Harga tidak boleh kosong"
            Toast.makeText(this, "Harga tidak boleh kosong", Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }

        val harga = hargaStr.toDoubleOrNull()
        if (harga == null || harga <= 0) {
            etHarga.error = "Harga harus lebih dari 0"
            Toast.makeText(this, "Harga harus lebih dari 0", Toast.LENGTH_SHORT).show()
            etHarga.requestFocus()
            return
        }

        if (cabang.isEmpty()) {
            etCabang.error = this.getString(R.string.validasiCabang)
            Toast.makeText(this, this.getString(R.string.validasiCabang), Toast.LENGTH_SHORT).show()
            etCabang.requestFocus()
            return
        }

        simpan()
    }

    fun simpan() {
        val tambahanBaru = myRef.push()
        val tambahanId = tambahanBaru.key
        val data = ModelTambahan(
            tambahanId.toString(),
            etNama.text.toString().trim(),
            etHarga.text.toString().trim(),
            etCabang.text.toString().trim()
        )

        tambahanBaru.setValue(data).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.suksestambahan), Toast.LENGTH_SHORT).show()

            // Logika baru untuk orientasi
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                clearForm() // Opsional: bersihkan form sebelum finish
                finish() // Tutup aktivitas jika dalam mode potret
            } else {
                // Dalam mode lanskap, bersihkan form dan muat ulang data di RecyclerView (jika ada)
                clearForm()
                if (rvDataTambahan != null) {
                    loadDataTambahan()
                }
            }
//            Toast.makeText(this, this.getString(R.string.suksestambahan), Toast.LENGTH_SHORT).show()
//            clearForm()
////            finish()
        }.addOnFailureListener {
            Toast.makeText(this, this.getString(R.string.gagaltambahan), Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearForm() {
        etNama.setText("")
        etHarga.setText("")
        etCabang.setText("")
    }
}