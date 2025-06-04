package com.huurisha.laundry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.adapter.KonfirmasiDataAdapter
import com.huurisha.laundry.modeldata.ModelLaporan
import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class konfirmasiData : AppCompatActivity() {
    private lateinit var rvKonfirmasi: RecyclerView
    private lateinit var tvnama: TextView
    private lateinit var tvhp: TextView
    private lateinit var tvnamalayanan: TextView
    private lateinit var tvhargalayanan: TextView
    private lateinit var hargatotal: TextView
    private lateinit var btnBatal: Button
    private lateinit var btnPembayaran: Button

    private var layananTambahanList = arrayListOf<ModelTransaksiTambahan>()

    // Variabel untuk menyimpan data
    private var nama: String? = null
    private var telepon: String? = null
    private var namalayanan: String? = null
    private var hargalayanan: Int = 0
    private var totalAkhir: Int = 0
    private var employeeName: String? = null
    private var transactionId: String? = null

    // Fungsi untuk memformat harga ke format Indonesia
    private fun formatHarga(harga: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(harga)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_konfirmasi_data)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()

        // Ambil data dari intent
        nama = intent.getStringExtra("nama")
        telepon = intent.getStringExtra("telepon")
        namalayanan = intent.getStringExtra("namalayanan")
        hargalayanan = intent.getIntExtra("hargalayanan", 0)
        employeeName = intent.getStringExtra("employee_name")
        val tambahanNamaList = intent.getStringArrayListExtra("tambahan_nama")
        val tambahanHargaList = intent.getIntegerArrayListExtra("tambahan_harga")

        // Generate transaction ID
        transactionId = "-O${System.currentTimeMillis()}"

        // Masukkan ke TextView dengan format harga yang benar
        tvnama.text = nama
        tvhp.text = telepon
        tvnamalayanan.text = namalayanan
        tvhargalayanan.text = formatHarga(hargalayanan)

        // Siapkan data layanan tambahan
        if (tambahanNamaList != null && tambahanHargaList != null) {
            for (i in tambahanNamaList.indices) {
                val item = ModelTransaksiTambahan(
                    nama = tambahanNamaList[i],
                    harga = tambahanHargaList[i]
                )
                layananTambahanList.add(item)
            }
        }

        // Hitung total
        val totalTambahan = layananTambahanList.sumOf { it.harga ?: 0 }
        totalAkhir = hargalayanan + totalTambahan
        hargatotal.text = formatHarga(totalAkhir)

        // Setup RecyclerView
        rvKonfirmasi.layoutManager = LinearLayoutManager(this)
        rvKonfirmasi.adapter = KonfirmasiDataAdapter(layananTambahanList)
    }

    private fun init() {
        rvKonfirmasi = findViewById(R.id.rvDatalkonfirmasi)
        tvnama = findViewById(R.id.nama)
        tvhp = findViewById(R.id.telepon)
        tvnamalayanan = findViewById(R.id.namalayanan)
        tvhargalayanan = findViewById(R.id.hargalayanan)
        hargatotal = findViewById(R.id.rp)
        btnBatal = findViewById(R.id.btnBatal)
        btnBatal.setOnClickListener {
            val intent = Intent()
            Toast.makeText(this,"Batal Memilih Pelanggan", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED, intent)
            finish()
        }

        btnPembayaran = findViewById(R.id.btnBayar)
        btnPembayaran.setOnClickListener {
            showPaymentDialog()
        }
    }

    private fun showPaymentDialog() {
        val dialogView = layoutInflater.inflate(R.layout.metode_pembayaran, null)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Setup tombol-tombol pembayaran
        val btnBayarNanti = dialogView.findViewById<Button>(R.id.btnBayarNanti)
        val btnTunai = dialogView.findViewById<Button>(R.id.btnTunai)
        val btnQRIS = dialogView.findViewById<Button>(R.id.btnQRIS)
        val btnDana = dialogView.findViewById<Button>(R.id.btnDana)
        val btnGopay = dialogView.findViewById<Button>(R.id.btnGopay)
        val btnOvo = dialogView.findViewById<Button>(R.id.btnOvo)
        val tvBatalDialog = dialogView.findViewById<TextView>(R.id.tvBatalDialog)

        btnBayarNanti.setOnClickListener {
            saveToDatabase("Bayar Nanti", "Belum dibayar")
            goToInvoice("Bayar Nanti")
            dialog.dismiss()
        }

        btnTunai.setOnClickListener {
            saveToDatabase("Tunai", "Sudah dibayar")
            goToInvoice("Tunai")
            dialog.dismiss()
        }

        btnQRIS.setOnClickListener {
            saveToDatabase("QRIS", "Sudah dibayar")
            goToInvoice("QRIS")
            dialog.dismiss()
        }

        btnDana.setOnClickListener {
            saveToDatabase("DANA", "Sudah dibayar")
            goToInvoice("DANA")
            dialog.dismiss()
        }

        btnGopay.setOnClickListener {
            saveToDatabase("GoPay", "Sudah dibayar")
            goToInvoice("GoPay")
            dialog.dismiss()
        }

        btnOvo.setOnClickListener {
            saveToDatabase("OVO", "Sudah dibayar")
            goToInvoice("OVO")
            dialog.dismiss()
        }

        tvBatalDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveToDatabase(metodePembayaran: String, statusPembayaran: String) {
        val database = FirebaseDatabase.getInstance().getReference("laporan")

        val tambahLayananText = if (layananTambahanList.isNotEmpty()) {
            "+${layananTambahanList.size} Layanan Tambahan"
        } else {
            "Tidak ada layanan tambahan"
        }

        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        if (transactionId.isNullOrEmpty()) {
            transactionId = "-O${System.currentTimeMillis()}"
        }

        val laporan = ModelLaporan(
            namapelangganlaporan = nama ?: "",
            terdafter = currentDateTime,
            namalayananlaporan = namalayanan ?: "",
            tambahlaporan = tambahLayananText,
            totalbayar = formatHarga(totalAkhir),
            metodepembayaran = metodePembayaran,
            statuspembayaran = statusPembayaran,
            transactionId = transactionId  // Tambahkan ini
        )

        database.child(transactionId!!).setValue(laporan)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToInvoice(metodePembayaran: String) {
        val intent = Intent(this, invoice_new::class.java)

        // Kirim semua data ke invoice
        intent.putExtra("customer_name", nama)
        intent.putExtra("customer_phone", telepon)
        intent.putExtra("service_name", namalayanan)
        intent.putExtra("service_price", hargalayanan)
        intent.putExtra("payment_method", metodePembayaran)
        intent.putExtra("total_price", totalAkhir)
        intent.putExtra("employee_name", employeeName ?: "Karina")

        // Kirim data layanan tambahan
        val tambahanNamaArray = ArrayList<String>()
        val tambahanHargaArray = ArrayList<Int>()

        for (item in layananTambahanList) {
            tambahanNamaArray.add(item.nama ?: "")
            tambahanHargaArray.add(item.harga ?: 0)
        }

        intent.putStringArrayListExtra("tambahan_nama", tambahanNamaArray)
        intent.putIntegerArrayListExtra("tambahan_harga", tambahanHargaArray)
        intent.putExtra("transaction_id", transactionId)

        startActivity(intent)
        finish()
    }
}