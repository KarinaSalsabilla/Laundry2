package com.huurisha.laundry

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.text.SimpleDateFormat
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.google.firebase.database.*
import com.huurisha.laundry.cabang.Data_cabang
import com.huurisha.laundry.layanan.dataLayanan
import com.huurisha.laundry.pegawai.dataPegawai
import com.huurisha.laundry.pelanggan.dataPelanggan
import com.huurisha.laundry.tambahan.datatambahan
import com.huurisha.laundry.transaksi.Transaksi
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var pelanggan1: ImageButton
    private lateinit var pegawai1: ImageView
    private lateinit var layanan: CardView
    private lateinit var tambahan: CardView
    private lateinit var cabang: ImageButton
    private lateinit var transaksi: ImageView
    private lateinit var laporan: ImageButton
    private lateinit var akun: ImageView

    // TextView untuk menampilkan data laporan
    private lateinit var rpTextView: TextView
    private lateinit var empat: TextView

    private var nama: String? = null
    private var noHp: String? = null
    private var alamat: String? = null

    // Firebase reference untuk real-time data
    private lateinit var firebaseDatabase: DatabaseReference
    private var laporanListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        init()

        // Ambil data dari intent login
        val nama = intent.getStringExtra("nama")
        val noHp = intent.getStringExtra("noHp")
        val alamat = intent.getStringExtra("alamat")

        this.nama = nama
        this.noHp = noHp
        this.alamat = alamat

        tekan()
        setupWindowInsets()

        // Setup Firebase listener untuk real-time updates
        setupFirebaseListener()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data laporan setiap kali activity kembali aktif
        loadDataLaporan()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listener untuk mencegah memory leak
        laporanListener?.let {
            firebaseDatabase.removeEventListener(it)
        }
    }

    private fun setupFirebaseListener() {
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("laporan")

        laporanListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Ketika ada perubahan data di Firebase, update tampilan
                loadDataLaporan()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }

        firebaseDatabase.addValueEventListener(laporanListener!!)
    }

    private fun setupWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val displayCutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val leftPadding = maxOf(systemBarsInsets.left, displayCutoutInsets.left)
            val topPadding = maxOf(systemBarsInsets.top, displayCutoutInsets.top)
            val rightPadding = maxOf(systemBarsInsets.right, displayCutoutInsets.right)
            val bottomPadding = maxOf(systemBarsInsets.bottom, imeInsets.bottom, displayCutoutInsets.bottom)

            view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
            windowInsets
        }
    }

    private fun init() {
        try {
            pelanggan1 = findViewById(R.id.pelanggan1)
            pegawai1 = findViewById(R.id.pegawai)
            layanan = findViewById(R.id.layanan)
            tambahan = findViewById(R.id.tambahan)
            cabang = findViewById(R.id.cabang2)
            transaksi = findViewById(R.id.tras)
            laporan = findViewById(R.id.imagebuttonlaporan)
            akun = findViewById(R.id.gb1)

            rpTextView = findViewById(R.id.rp)
            empat = findViewById(R.id.empat)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadDataLaporan() {
        val calendar = Calendar.getInstance()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Load data dari Firebase
        loadDataFromFirebase(today)

        // Juga load dari SQLite jika masih digunakan
        loadDataFromSQLite(today)
    }

    private fun loadDataFromFirebase(today: String) {
        firebaseDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPendapatanHari = 0.0
                var jumlahTransaksiHari = 0

                for (childSnapshot in snapshot.children) {
                    try {
                        val tanggal = childSnapshot.child("terdafter").getValue(String::class.java) ?: ""
                        val status = childSnapshot.child("statuspembayaran").getValue(String::class.java) ?: ""
                        val totalBayarStr = childSnapshot.child("totalbayar").getValue(String::class.java) ?: "Rp 0"

                        // Ekstrak tanggal saja (yyyy-MM-dd) dari datetime
                        val tanggalOnly = if (tanggal.length >= 10) tanggal.substring(0, 10) else tanggal

                        // Hanya hitung transaksi yang sudah dibayar dan hari ini
                        if (tanggalOnly == today && (status == "Sudah dibayar" || status == "Sudah diambil")) {
                            // Parse total bayar dengan lebih hati-hati
                            val harga = parseHargaString(totalBayarStr)
                            totalPendapatanHari += harga
                            jumlahTransaksiHari++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Update UI di main thread
                runOnUiThread {
                    updatePendapatanUI(totalPendapatanHari)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                runOnUiThread {
                    rpTextView.text = "Rp."
                    empat.text = "0,-"
                }
            }
        })
    }

    private fun loadDataFromSQLite(today: String) {
        try {
            val dbHelper = DatabaseHelper(this)
            val pendapatanHari = dbHelper.getTotalPendapatanHari(today)

            // Jika SQLite masih digunakan, gabungkan dengan data Firebase
            // Atau gunakan salah satu saja sesuai kebutuhan

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseHargaString(hargaStr: String): Double {
        return try {
            // Contoh format yang mungkin ada:
            // "Rp 68.000,00", "Rp 15.000,-", "Rp15000", "15000", etc.

            var cleanPrice = hargaStr
                .replace("Rp", "", true) // Hapus Rp (case insensitive)
                .replace("-", "") // Hapus tanda minus di akhir
                .replace(" ", "") // Hapus spasi
                .trim()

            // Jika string kosong setelah dibersihkan, return 0
            if (cleanPrice.isEmpty()) return 0.0

            // Deteksi format Indonesia (titik sebagai pemisah ribuan, koma sebagai desimal)
            // Contoh: "68.000,00" atau "68.000"
            return if (cleanPrice.contains(".") && cleanPrice.contains(",")) {
                // Format: 68.000,00 (ada titik dan koma)
                val parts = cleanPrice.split(",")
                val integerPart = parts[0].replace(".", "") // Hapus titik pemisah ribuan
                val decimalPart = if (parts.size > 1) parts[1] else "0"
                "$integerPart.$decimalPart".toDoubleOrNull() ?: 0.0
            } else if (cleanPrice.contains(".") && !cleanPrice.contains(",")) {
                // Bisa jadi format: 68.000 (titik sebagai pemisah ribuan) atau 68.50 (titik sebagai desimal)
                val dotIndex = cleanPrice.lastIndexOf(".")
                val afterDot = cleanPrice.substring(dotIndex + 1)

                if (afterDot.length <= 2 && cleanPrice.count { it == '.' } == 1) {
                    // Kemungkinan desimal (contoh: 68.50)
                    cleanPrice.toDoubleOrNull() ?: 0.0
                } else {
                    // Kemungkinan pemisah ribuan (contoh: 68.000)
                    cleanPrice.replace(".", "").toDoubleOrNull() ?: 0.0
                }
            } else if (cleanPrice.contains(",")) {
                // Format dengan koma sebagai desimal tanpa titik: 68000,00
                cleanPrice.replace(",", ".").toDoubleOrNull() ?: 0.0
            } else {
                // Format angka biasa tanpa pemisah: 68000
                cleanPrice.toDoubleOrNull() ?: 0.0
            }

        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    private fun updatePendapatanUI(totalPendapatan: Double) {
        try {
            // Jika totalPendapatan adalah 0, tampilkan 0 saja
            if (totalPendapatan == 0.0) {
                rpTextView.text = ""
                empat.text = "Rp 0,00"
                return
            }

            // Format angka ke format Indonesia (titik sebagai pemisah ribuan, koma sebagai desimal)
            val formatter = NumberFormat.getInstance(Locale("id", "ID"))
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2

            val formattedNumber = formatter.format(totalPendapatan)

            rpTextView.text = ""
            empat.text = "Rp $formattedNumber"

        } catch (e: Exception) {
            e.printStackTrace()
            rpTextView.text = ""
            empat.text = "Rp 0,00"
        }
    }

    private fun tekan() {
        pelanggan1.setOnClickListener {
            val intent = Intent(this, dataPelanggan::class.java)
            startActivity(intent)
        }

        pegawai1.setOnClickListener {
            val intent = Intent(this, dataPegawai::class.java)
            startActivity(intent)
        }

        layanan.setOnClickListener {
            val intent = Intent(this, dataLayanan::class.java)
            startActivity(intent)
        }

        tambahan.setOnClickListener {
            val intent = Intent(this, datatambahan::class.java)
            startActivity(intent)
        }

        cabang.setOnClickListener {
            val intent = Intent(this, Data_cabang::class.java)
            startActivity(intent)
        }

        transaksi.setOnClickListener {
            val intent = Intent(this, Transaksi::class.java)
            startActivity(intent)
        }

        laporan.setOnClickListener {
            val intent = Intent(this, data_laporan::class.java)
            startActivity(intent)
        }

        akun.setOnClickListener {
            val intent = Intent(this, Akun::class.java)
            startActivity(intent)
        }

        setupGreetingAndDate()
    }

    private fun setupGreetingAndDate() {
        try {
            val helloTextView = findViewById<TextView>(R.id.halo)
            val dateTextView = findViewById<TextView>(R.id.tanggal)

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(calendar.time)
            dateTextView.text = currentDate

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour in 5..11 -> this.getString(R.string.pagi)
                hour in 12..14 -> this.getString(R.string.siang)
                hour in 15..17 -> this.getString(R.string.sore)
                else -> this.getString(R.string.malam)
            }
            helloTextView.text = greeting
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // DatabaseHelper tetap untuk backward compatibility
    class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "laundry.db", null, 1) {

        override fun onCreate(db: android.database.sqlite.SQLiteDatabase?) {
            val createTableQuery = """
            CREATE TABLE IF NOT EXISTS transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tanggal TEXT,
                total_harga REAL
            )
        """.trimIndent()
            db?.execSQL(createTableQuery)
        }

        override fun onUpgrade(
            db: android.database.sqlite.SQLiteDatabase?,
            oldVersion: Int,
            newVersion: Int
        ) {
            db?.execSQL("DROP TABLE IF EXISTS transaksi")
            onCreate(db)
        }

        fun getTotalPendapatanHari(tanggal: String): Double {
            val db = readableDatabase
            var total = 0.0
            var cursor: android.database.Cursor? = null

            try {
                cursor = db.rawQuery(
                    "SELECT SUM(total_harga) FROM transaksi WHERE DATE(tanggal) = ?",
                    arrayOf(tanggal)
                )
                if (cursor.moveToFirst()) {
                    total = cursor.getDouble(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return total
        }

        fun getJumlahTransaksiHari(tanggal: String): Int {
            val db = readableDatabase
            var jumlah = 0
            var cursor: android.database.Cursor? = null

            try {
                cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM transaksi WHERE DATE(tanggal) = ?",
                    arrayOf(tanggal)
                )
                if (cursor.moveToFirst()) {
                    jumlah = cursor.getInt(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return jumlah
        }

        fun getTotalPendapatanBulan(bulan: String): Double {
            val db = readableDatabase
            var total = 0.0
            var cursor: android.database.Cursor? = null

            try {
                cursor = db.rawQuery(
                    "SELECT SUM(total_harga) FROM transaksi WHERE strftime('%Y-%m', tanggal) = ?",
                    arrayOf(bulan)
                )
                if (cursor.moveToFirst()) {
                    total = cursor.getDouble(0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
            return total
        }
    }
}