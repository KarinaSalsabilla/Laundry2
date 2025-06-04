package com.huurisha.laundry

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.huurisha.laundry.adapter.DataLaporanAdapter
import com.huurisha.laundry.modeldata.ModelLaporan
import java.text.SimpleDateFormat
import java.util.*

class data_laporan : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataLaporanAdapter
    private lateinit var listLaporan: ArrayList<ModelLaporan>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_laporan)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        database = FirebaseDatabase.getInstance().reference.child("laporan")
        loadData()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rvDataLayanan)
        recyclerView.layoutManager = LinearLayoutManager(this)
        listLaporan = ArrayList()

        // Inisialisasi adapter dengan callback untuk handle aksi button
        adapter = DataLaporanAdapter(listLaporan) { item, action ->
            handleAdapterCallback(item, action)
        }

        recyclerView.adapter = adapter
    }

    private fun handleAdapterCallback(item: ModelLaporan, action: String) {
        when (action) {
            "PAYMENT_CONFIRMED" -> {
                // Aksi ketika pembayaran dikonfirmasi
                val message = if (item.metodepembayaran.isNullOrEmpty()) {
                    "Pembayaran untuk ${item.namapelangganlaporan} telah dikonfirmasi"
                } else {
                    "Pembayaran untuk ${item.namapelangganlaporan} via ${item.metodepembayaran} telah dikonfirmasi"
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Log untuk debugging
                Log.d("DataLaporan", "Payment confirmed for: ${item.namapelangganlaporan}")
            }
            "ORDER_PICKED_UP" -> {
                // Aksi ketika pesanan diambil
                val currentDateTime = getCurrentDateTime()
                Toast.makeText(
                    this,
                    "Pesanan ${item.namapelangganlaporan} telah diambil pada $currentDateTime",
                    Toast.LENGTH_LONG
                ).show()

                // Log untuk debugging
                Log.d("DataLaporan", "Order picked up: ${item.namapelangganlaporan}")

                // Bisa tambahkan aksi lain seperti:
                // - Kirim notifikasi
                // - Update statistik
                // - Generate laporan
            }
        }
    }

    private fun loadData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLaporan.clear()

                if (!snapshot.exists()) {
                    Toast.makeText(this@data_laporan, "Tidak ada data laporan", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                    return
                }

                for (data in snapshot.children) {
                    val laporan = data.getValue(ModelLaporan::class.java)

                    if (laporan != null) {
                        // Set transaction ID dari key Firebase
                        laporan.transactionId = data.key
                        listLaporan.add(laporan)

                        // Log untuk debugging
                        Log.d("DataLaporan", "Loaded item: ${laporan.namapelangganlaporan}, Status: ${laporan.statuspembayaran}, ID: ${laporan.transactionId}")
                    }
                }

                // Sort berdasarkan tanggal terbaru (terdafter)
                try {
                    listLaporan.sortByDescending {
                        parseDate(it.terdafter ?: "")
                    }
                } catch (e: Exception) {
                    Log.e("data_laporan", "Error sorting data: ${e.message}")
                    // Jika sorting gagal, tetap tampilkan data tanpa sort
                }

                adapter.notifyDataSetChanged()

                Log.d("DataLaporan", "Data loaded: ${listLaporan.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("data_laporan", "Database error: ${error.message}")
                Toast.makeText(this@data_laporan, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun parseDate(dateString: String): Date {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            format.parse(dateString) ?: Date(0)
        } catch (e: Exception) {
            Date(0) // Return epoch time if parsing fails
        }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener untuk mencegah memory leak
        database.removeEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}