package com.huurisha.laundry.layanan

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataLayananAdapter // Ganti dengan adapter yang sesuai
import com.huurisha.laundry.modeldata.ModelLayanan

class tambahLayanan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var tvJudul: TextView
    lateinit var etNama: EditText
    lateinit var etHarga: EditText
    lateinit var etCabang: EditText
    lateinit var btSimpan: Button

    var rvDataLayanan: RecyclerView? = null // Ganti nama variabel
    var layananAdapter: DataLayananAdapter? = null // Ganti dengan adapter yang sesuai
    private val listLayanan = arrayListOf<ModelLayanan>() // Ganti nama list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_layanan)

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
        tvJudul = findViewById(R.id.tvlayanan)
        etNama = findViewById(R.id.inputnamalayanan)
        etHarga = findViewById(R.id.inputharga)
        etCabang = findViewById(R.id.inputcabang)
        btSimpan = findViewById(R.id.simpan)

        // Fix: Gunakan ID yang benar dari XML
        val recyclerView = findViewById<RecyclerView>(R.id.rvDataLayanan)
        if (recyclerView != null) {
            rvDataLayanan = recyclerView
            setupRecyclerView()
            loadDataLayanan() // Ganti nama fungsi
        }
    }

    fun setupRecyclerView() {
        rvDataLayanan?.let { rv ->
            layananAdapter = DataLayananAdapter(this,listLayanan) // Ganti dengan adapter yang sesuai
            rv.adapter = layananAdapter
            rv.layoutManager = LinearLayoutManager(this)
        }
    }

    fun loadDataLayanan() { // Ganti nama fungsi
        // Hanya jalankan jika RecyclerView ada
        if (rvDataLayanan != null) {
            myRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listLayanan.clear()
                    for (data in snapshot.children) {
                        // Fix: Gunakan ModelLayanan yang sesuai
                        val layanan = data.getValue(ModelLayanan::class.java)
                        layanan?.let { listLayanan.add(it) }
                    }
                    layananAdapter?.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Fix: Gunakan nama class yang benar
                    Toast.makeText(this@tambahLayanan, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun cekValidasi() {
        val nama = etNama.text.toString()
        val harga = etHarga.text.toString()
        val cabang = etCabang.text.toString()

        if (nama.isEmpty()) {
            etNama.error = this.getString(R.string.validasinama)
            Toast.makeText(this, this.getString(R.string.validasinama), Toast.LENGTH_SHORT).show()
            etNama.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            etHarga.error = this.getString(R.string.validasiHarga)
            Toast.makeText(this, this.getString(R.string.validasiHarga), Toast.LENGTH_SHORT).show()
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
        val layananBaru = myRef.push()
        val layananId = layananBaru.key
        val data = ModelLayanan(
            layananId.toString(),
            etNama.text.toString(),
            etHarga.text.toString(),
            etCabang.text.toString()
        )
        layananBaru.setValue(data).addOnSuccessListener {
            // Logika baru untuk orientasi
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                clearForm() // Opsional: bersihkan form sebelum finish
                finish() // Tutup aktivitas jika dalam mode potret
            } else {
                // Dalam mode lanskap, bersihkan form dan muat ulang data di RecyclerView (jika ada)
                clearForm()
                if (rvDataLayanan != null) {
                    loadDataLayanan() // Ganti nama fungsi
                }
            }
        }

//            Toast.makeText(this, this.getString(R.string.sukseslayanan), Toast.LENGTH_SHORT).show()
//            // Hanya refresh jika RecyclerView ada (landscape)
//            if (rvDataLayanan != null) {
//                loadDataLayanan() // Ganti nama fungsi
//            }
//            clearForm()
//        }
            .addOnFailureListener {
                Toast.makeText(this, this.getString(R.string.gagalayanan), Toast.LENGTH_SHORT).show()
            }
    }

    // Fix: Tambahkan fungsi clearForm yang hilang
    private fun clearForm() {
        etNama.setText("")
        etHarga.setText("")
        etCabang.setText("")
        etNama.requestFocus()
    }
}