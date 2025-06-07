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

        // Set title with translation
        title = getString(R.string.data_laporan_title)

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
                    getString(R.string.payment_confirmed_simple, item.namapelangganlaporan ?: "")
                } else {
                    getString(R.string.payment_confirmed_with_method,
                        item.namapelangganlaporan ?: "",
                        item.metodepembayaran ?: "")
                }

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Log untuk debugging
                Log.d("DataLaporan", getString(R.string.payment_confirmed_log, item.namapelangganlaporan ?: ""))
            }
            "ORDER_PICKED_UP" -> {
                // Aksi ketika pesanan diambil
                val currentDateTime = getCurrentDateTime()
                val message = getString(R.string.order_picked_up,
                    item.namapelangganlaporan ?: "",
                    currentDateTime)

                Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                // Log untuk debugging
                Log.d("DataLaporan", getString(R.string.order_picked_up_log, item.namapelangganlaporan ?: ""))

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
                    Toast.makeText(this@data_laporan, getString(R.string.no_report_data), Toast.LENGTH_SHORT).show()
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
                        Log.d("DataLaporan", getString(R.string.loaded_item_log,
                            laporan.namapelangganlaporan ?: "",
                            laporan.statuspembayaran ?: "",
                            laporan.transactionId ?: ""))
                    }
                }

                // Sort berdasarkan tanggal terbaru (terdafter)
                try {
                    listLaporan.sortByDescending {
                        parseDate(it.terdafter ?: "")
                    }
                } catch (e: Exception) {
                    Log.e("data_laporan", getString(R.string.error_sorting_log, e.message ?: ""))
                    // Jika sorting gagal, tetap tampilkan data tanpa sort
                }

                adapter.notifyDataSetChanged()

                Log.d("DataLaporan", getString(R.string.data_loaded_log, listLaporan.size))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("data_laporan", getString(R.string.database_error_log, error.message))
                Toast.makeText(this@data_laporan,
                    getString(R.string.failed_load_data, error.message),
                    Toast.LENGTH_SHORT).show()
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