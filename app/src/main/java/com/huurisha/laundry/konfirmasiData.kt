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
    private var transactionId: String? = null // This will hold the Firebase push ID

    // Konstanta status untuk konsistensi dengan adapter
    companion object {
        const val STATUS_UNPAID = "Belum dibayar"
        const val STATUS_PAID = "Sudah dibayar"
        const val STATUS_COMPLETED = "Sudah diambil"
    }

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
        loadDataFromIntent()
        setupRecyclerView()
        calculateTotal()
    }

    private fun init() {
        rvKonfirmasi = findViewById(R.id.rvDatalkonfirmasi)
        tvnama = findViewById(R.id.nama)
        tvhp = findViewById(R.id.telepon)
        tvnamalayanan = findViewById(R.id.namalayanan)
        tvhargalayanan = findViewById(R.id.hargalayanan)
        hargatotal = findViewById(R.id.rp)
        btnBatal = findViewById(R.id.btnBatal)
        btnPembayaran = findViewById(R.id.btnBayar)

        btnBatal.setOnClickListener {
            // Use string resource for Toast message
            Toast.makeText(this, getString(R.string.btn_cancel), Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
        }

        btnPembayaran.setOnClickListener {
            showPaymentDialog()
        }
    }

    private fun loadDataFromIntent() {
        nama = intent.getStringExtra("nama")
        telepon = intent.getStringExtra("telepon")
        namalayanan = intent.getStringExtra("namalayanan")
        hargalayanan = intent.getIntExtra("hargalayanan", 0)
        employeeName = intent.getStringExtra("employee_name")

        val tambahanNamaList = intent.getStringArrayListExtra("tambahan_nama")
        val tambahanHargaList = intent.getIntegerArrayListExtra("tambahan_harga")

        tvnama.text = nama ?: "-"
        tvhp.text = telepon ?: "-"
        tvnamalayanan.text = namalayanan ?: "-"
        tvhargalayanan.text = formatHarga(hargalayanan)

        prepareAdditionalServices(tambahanNamaList, tambahanHargaList)
    }

    private fun prepareAdditionalServices(namaList: ArrayList<String>?, hargaList: ArrayList<Int>?) {
        layananTambahanList.clear()

        if (namaList != null && hargaList != null && namaList.size == hargaList.size) {
            for (i in namaList.indices) {
                val item = ModelTransaksiTambahan(
                    nama = namaList[i],
                    harga = hargaList[i]
                )
                layananTambahanList.add(item)
            }
        }
    }

    private fun setupRecyclerView() {
        rvKonfirmasi.layoutManager = LinearLayoutManager(this)
        rvKonfirmasi.adapter = KonfirmasiDataAdapter(layananTambahanList)
    }

    private fun calculateTotal() {
        val totalTambahan = layananTambahanList.sumOf { it.harga ?: 0 }
        totalAkhir = hargalayanan + totalTambahan
        hargatotal.text = formatHarga(totalAkhir)
    }

    private fun showPaymentDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.metode_pembayaran, null)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create()

            setupPaymentButtons(dialogView, dialog)
            dialog.show()

        } catch (e: Exception) {
            // Use string resource with a placeholder for the error message
            Toast.makeText(this, getString(R.string.error_displaying_payment_dialog, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupPaymentButtons(dialogView: android.view.View, dialog: androidx.appcompat.app.AlertDialog) {
        val btnBayarNanti = dialogView.findViewById<Button>(R.id.btnBayarNanti)
        val btnTunai = dialogView.findViewById<Button>(R.id.btnTunai)
        val btnQRIS = dialogView.findViewById<Button>(R.id.btnQRIS)
        val btnDana = dialogView.findViewById<Button>(R.id.btnDana)
        val btnGopay = dialogView.findViewById<Button>(R.id.btnGopay)
        val btnOvo = dialogView.findViewById<Button>(R.id.btnOvo)
        val tvBatalDialog = dialogView.findViewById<TextView>(R.id.tvBatalDialog)

        btnBayarNanti?.setOnClickListener {
            processPayment("Pay Later", STATUS_UNPAID, dialog)
        }

        btnTunai?.setOnClickListener {
            processPayment("Tunai", STATUS_PAID, dialog)
        }

        btnQRIS?.setOnClickListener {
            processPayment("QRIS", STATUS_PAID, dialog)
        }

        btnDana?.setOnClickListener {
            processPayment("DANA", STATUS_PAID, dialog)
        }

        btnGopay?.setOnClickListener {
            processPayment("GoPay", STATUS_PAID, dialog)
        }

        btnOvo?.setOnClickListener {
            processPayment("OVO", STATUS_PAID, dialog)
        }

        tvBatalDialog?.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun processPayment(metodePembayaran: String, statusPembayaran: String, dialog: androidx.appcompat.app.AlertDialog) {
        dialog.window?.decorView?.isEnabled = false

        try {
            saveToDatabase(metodePembayaran, statusPembayaran) { success ->
                if (success) {
                    goToInvoice(metodePembayaran, statusPembayaran)
                    dialog.dismiss()
                } else {
                    dialog.window?.decorView?.isEnabled = true
                }
            }
        } catch (e: Exception) {
            // Use string resource with a placeholder for the error message
            Toast.makeText(this, getString(R.string.error_processing_payment, e.message), Toast.LENGTH_SHORT).show()
            dialog.window?.decorView?.isEnabled = true
        }
    }

    private fun saveToDatabase(metodePembayaran: String, statusPembayaran: String, callback: (Boolean) -> Unit) {
        try {
            val database = FirebaseDatabase.getInstance().getReference("laporan")

            val tambahLayananText = if (layananTambahanList.isNotEmpty()) {
                getString(R.string.layanantambahan) + " (${layananTambahanList.size})"
            } else {
                getString(R.string.no_additional_service)
            }

            val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            val newTransactionRef = database.push()
            transactionId = newTransactionRef.key

            val laporan = ModelLaporan(
                namapelangganlaporan = nama ?: "",
                terdafter = currentDateTime,
                namalayananlaporan = namalayanan ?: "",
                tambahlaporan = tambahLayananText,
                totalbayar = formatHarga(totalAkhir),
                metodepembayaran = metodePembayaran,
                statuspembayaran = statusPembayaran,
                transactionId = transactionId
            )

            newTransactionRef.setValue(laporan)
                .addOnSuccessListener {
                    // Use string resource
                    Toast.makeText(this, getString(R.string.data_saved_successfully), Toast.LENGTH_SHORT).show()
                    callback(true)
                }
                .addOnFailureListener { exception ->
                    // Use string resource with a placeholder for the error message
                    Toast.makeText(this, getString(R.string.failed_to_save_data, exception.message), Toast.LENGTH_SHORT).show()
                    callback(false)
                }

        } catch (e: Exception) {
            // Use string resource with a placeholder for the error message
            Toast.makeText(this, getString(R.string.failed_to_save_data, e.message), Toast.LENGTH_SHORT).show()
            callback(false)
        }
    }

    private fun goToInvoice(metodePembayaran: String, statusPembayaran: String) {
        try {
            val intent = Intent(this, invoice_new::class.java)

            intent.putExtra("customer_name", nama)
            intent.putExtra("customer_phone", telepon)
            intent.putExtra("service_name", namalayanan)
            intent.putExtra("service_price", hargalayanan)
            intent.putExtra("payment_method", metodePembayaran)
            intent.putExtra("payment_status", statusPembayaran)
            intent.putExtra("total_price", totalAkhir)
            intent.putExtra("employee_name", employeeName ?: "Karina")
            intent.putExtra("transaction_id", transactionId)

            val tambahanNamaArray = ArrayList<String>()
            val tambahanHargaArray = ArrayList<Int>()

            for (item in layananTambahanList) {
                tambahanNamaArray.add(item.nama ?: "")
                tambahanHargaArray.add(item.harga ?: 0)
            }

            intent.putStringArrayListExtra("tambahan_nama", tambahanNamaArray)
            intent.putIntegerArrayListExtra("tambahan_harga", tambahanHargaArray)

            startActivity(intent)
            finish()

        } catch (e: Exception) {
            // Use string resource with a placeholder for the error message
            Toast.makeText(this, getString(R.string.error_opening_invoice, e.message), Toast.LENGTH_SHORT).show()
        }
    }
}