package com.huurisha.laundry

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintJob
import android.print.PrintManager
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.adapter.KonfirmasiDataAdapter
import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
import com.huurisha.laundry.modeldata.ModelInvoice
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// TAMBAHAN IMPORT UNTUK BLUETOOTH
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.NumberFormat

class invoice_new : AppCompatActivity() {

    // Firebase Database
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("invoice")

    // UI Components
    private lateinit var tvTransactionId: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvCustomerName: TextView
    private lateinit var tvEmployeeName: TextView
    private lateinit var tvServiceName: TextView
    private lateinit var tvServicePrice: TextView
    private lateinit var tvAdditionalSubtotal: TextView
    private lateinit var tvTotal: TextView
    private lateinit var rvTambahan: RecyclerView
    private lateinit var btnWhatsApp: Button
    private lateinit var btnPrint: Button

    // Root view untuk capture
    private lateinit var invoiceLayout: View

    // BLUETOOTH PRINTER - Menghapus BluetoothPrinterManager yang tidak didefinisikan
    private var selectedPrinter: BluetoothDevice? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // Ganti dengan MAC address printer Anda
    private val printerMAC = "DC:0D:51:A7:FF:7A"
    private val printerUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 100
        private const val TAG = "InvoiceActivity"
    }

    // Permission request launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Izin Bluetooth diberikan", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                "Permission Bluetooth diperlukan untuk mencetak",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Data variables
    private var layananTambahanList = arrayListOf<ModelTransaksiTambahan>()
    private var customerName: String? = null
    private var customerPhone: String? = null
    private var employeeName: String? = null
    private var transactionId: String? = null
    private var totalPrice: Int = 0
    private var servicePrice: Int = 0
    private var serviceName: String? = null
    private var currentDateTime: String? = null

    // Currency formatter
    private val currencyFormat: DecimalFormat = DecimalFormat("'Rp '#,##0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_invoice_new)

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            // Initialize bluetooth
            initializeBluetooth()

            initViews()
            loadDataFromIntent()
            setupRecyclerView()
            setupButtons()

            // Simpan invoice ke database
            saveInvoiceToDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(this, "Error initializing invoice: ${e.message}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun initViews() {
        try {
            // Mapping sesuai dengan ID di XML layout
            tvTransactionId = findViewById(R.id.transaksiInvoice)
            tvDateTime = findViewById(R.id.tanggaljam)
            tvCustomerName = findViewById(R.id.namaPelanggan)
            tvEmployeeName = findViewById(R.id.namaPegawai)
            tvServiceName = findViewById(R.id.namaLayanan)
            tvServicePrice = findViewById(R.id.hargaLayanan)
            tvAdditionalSubtotal = findViewById(R.id.totalTambahan)
            tvTotal = findViewById(R.id.totalBayar)
            rvTambahan = findViewById(R.id.rvDatalkonfirmasi)
            btnWhatsApp = findViewById(R.id.btnwa)
            btnPrint = findViewById(R.id.btnCetak)

            // Get root layout untuk capture
            invoiceLayout = findViewById(android.R.id.content)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            throw e
        }
    }

    private fun loadDataFromIntent() {
        try {
            // Ambil data dari intent
            customerName = intent.getStringExtra("customer_name")
            customerPhone = intent.getStringExtra("customer_phone")
            employeeName = intent.getStringExtra("employee_name")
            serviceName = intent.getStringExtra("service_name")
            servicePrice = intent.getIntExtra("service_price", 0)
            totalPrice = intent.getIntExtra("total_price", 0)
            transactionId = intent.getStringExtra("transaction_id")

            val tambahanNamaList = intent.getStringArrayListExtra("tambahan_nama")
            val tambahanHargaList = intent.getIntegerArrayListExtra("tambahan_harga")

            // Set data ke TextView dengan null safety
            tvTransactionId.text = transactionId ?: "TRX-${System.currentTimeMillis()}"
            tvCustomerName.text = customerName ?: "Customer"
            tvEmployeeName.text = employeeName ?: "Staff"
            tvServiceName.text = serviceName ?: "Layanan"
            tvServicePrice.text = currencyFormat.format(servicePrice)

            // Set tanggal dan jam saat ini
            currentDateTime =
                SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            tvDateTime.text = currentDateTime

            // Siapkan data layanan tambahan
            layananTambahanList.clear()
            if (tambahanNamaList != null && tambahanHargaList != null) {
                val minSize = minOf(tambahanNamaList.size, tambahanHargaList.size)
                for (i in 0 until minSize) {
                    val item = ModelTransaksiTambahan(
                        nama = tambahanNamaList[i],
                        harga = tambahanHargaList[i]
                    )
                    layananTambahanList.add(item)
                }
            }

            // Hitung subtotal tambahan
            val additionalSubtotal = layananTambahanList.sumOf { it.harga ?: 0 }
            tvAdditionalSubtotal.text = currencyFormat.format(additionalSubtotal)
            tvTotal.text = currencyFormat.format(totalPrice)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading data from intent", e)
            Toast.makeText(this, "Error loading invoice data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            rvTambahan.layoutManager = LinearLayoutManager(this)
            rvTambahan.adapter = KonfirmasiDataAdapter(layananTambahanList)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up RecyclerView", e)
        }
    }

    private fun setupButtons() {
        btnWhatsApp.setOnClickListener {
            try {
                sendWhatsAppMessage()
            } catch (e: Exception) {
                Log.e(TAG, "Error sending WhatsApp", e)
                Toast.makeText(this, "Error sending WhatsApp message", Toast.LENGTH_SHORT).show()
            }
        }

        btnPrint.setOnClickListener {
            try {
                handlePrintClick()
            } catch (e: Exception) {
                Log.e(TAG, "Error printing", e)
                Toast.makeText(this, "Error printing invoice", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Initialize Bluetooth
    private fun initializeBluetooth() {
        try {
            bluetoothAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val bluetoothManager =
                    getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager.adapter
            } else {
                @Suppress("DEPRECATION")
                BluetoothAdapter.getDefaultAdapter()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException initializing Bluetooth", e)
            showToast("Tidak dapat mengakses Bluetooth: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Bluetooth", e)
            showToast("Error initializing Bluetooth: ${e.message}")
        }
    }

    // Handle print click dengan permission check
    private fun handlePrintClick() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            // Tampilkan dialog untuk mengaktifkan Bluetooth
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Bluetooth Tidak Aktif")
                .setMessage("Aktifkan Bluetooth untuk melanjutkan pencetakan?")
                .setPositiveButton("Aktifkan") { _, _ ->
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(enableBtIntent)
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
            return
        }

        // Check permissions
        if (hasAllBluetoothPermissions()) {
            // Tampilkan dialog loading
            val loadingDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Mencetak...")
                .setMessage("Mohon tunggu, sedang menghubungkan ke printer...")
                .setCancelable(false)
                .create()

            loadingDialog.show()

            // Delay untuk UI responsiveness
            Handler(Looper.getMainLooper()).postDelayed({
                loadingDialog.dismiss()
                val message = buildPrintMessage()
                printToBluetooth(message)
            }, 500)
        } else {
            requestBluetoothPermissions()
        }
    }

    // Fungsi untuk cek semua permission Bluetooth
    private fun hasAllBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ memerlukan BLUETOOTH_CONNECT dan BLUETOOTH_SCAN
            val connectPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            val scanPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            connectPermission && scanPermission
        } else {
            // Android 11 dan dibawah
            val bluetoothPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED

            val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED

            bluetoothPermission && bluetoothAdminPermission
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Untuk Android dibawah 12, cek permission BLUETOOTH
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request Bluetooth permissions - Perbaiki fungsi yang duplikat
    private fun requestBluetoothPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH)
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (permissionsNeeded.isNotEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toTypedArray())
        }
    }

    private fun generateCustomId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = Random()
        val result = StringBuilder()

        result.append("-O")
        repeat(20) {
            result.append(chars[random.nextInt(chars.length)])
        }

        return result.toString()
    }

    // Simpan invoice ke database
    private fun saveInvoiceToDatabase() {
        try {
            val invoiceId = generateCustomId()

            val invoice = ModelInvoice(
                idInvoice = invoiceId,
                idTransaksi = transactionId,
                namaPelanggan = customerName,
                noHpPelanggan = customerPhone,
                namaPegawai = employeeName,
                namaLayanan = serviceName,
                hargaLayanan = servicePrice,
                layananTambahan = layananTambahanList,
                totalTambahan = layananTambahanList.sumOf { it.harga ?: 0 },
                totalBayar = totalPrice,
                tanggalWaktu = currentDateTime,
                timestamp = System.currentTimeMillis()
            )

            // Uncomment jika ingin menyimpan ke Firebase
            /*
            myRef.child(invoiceId).setValue(invoice)
                .addOnSuccessListener {
                    Toast.makeText(this, "Invoice berhasil disimpan: $invoiceId", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal menyimpan invoice: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            */
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to database", e)
        }
    }

    private fun sendWhatsAppMessage() {
        if (customerPhone.isNullOrEmpty()) {
            Toast.makeText(this, "Nomor HP pelanggan tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val message = createInvoiceMessage()

        try {
            val cleanNumber = cleanPhoneNumber(customerPhone!!)
            val whatsappNumber = formatWhatsAppNumber(cleanNumber)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}")

            val packageManager = packageManager
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "WhatsApp akan dibuka di browser", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening WhatsApp", e)
            Toast.makeText(this, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi createInvoiceMessage() yang hilang
    private fun createInvoiceMessage(): String {
        return buildString {
            append("*KAARYNS LAUNDRY*\n")
            append("Invoice Pembayaran\n\n")
            append("ID Transaksi: $transactionId\n")
            append("Tanggal: $currentDateTime\n")
            append("Pelanggan: $customerName\n")
            append("Pegawai: $employeeName\n\n")

            append("*Detail Layanan:*\n")
            append("$serviceName: ${currencyFormat.format(servicePrice)}\n")

            if (layananTambahanList.isNotEmpty()) {
                append("\n*Layanan Tambahan:*\n")
                layananTambahanList.forEach { item ->
                    append("${item.nama}: ${currencyFormat.format(item.harga ?: 0)}\n")
                }
            }

            append("\n*TOTAL BAYAR: ${currencyFormat.format(totalPrice)}*\n\n")
            append("Terima kasih telah memilih KAARYNS LAUNDRY!")
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }

    private fun formatWhatsAppNumber(phoneNumber: String): String {
        var cleanNumber = cleanPhoneNumber(phoneNumber)

        if (cleanNumber.startsWith("0")) {
            cleanNumber = "62" + cleanNumber.substring(1)
        } else if (cleanNumber.startsWith("+62")) {
            cleanNumber = cleanNumber.substring(1)
        } else if (!cleanNumber.startsWith("62")) {
            cleanNumber = "62$cleanNumber"
        }

        return cleanNumber
    }

    // Fungsi untuk membangun pesan print dengan formatting yang lebih baik
    private fun buildPrintMessage(): String {
        return buildString {
            // ESC/POS commands untuk thermal printer
            append("\u001B@") // Initialize printer
            append("\u001Ba\u0001") // Center alignment

            append("\n")
            append("KAARYNS LAUNDRY\n")
            append("Invoice Pembayaran\n")
            append("\u001Ba\u0000") // Left alignment
            append("================================\n")
            append("ID Transaksi: $transactionId\n")
            append("Tanggal: $currentDateTime\n")
            append("Pelanggan: $customerName\n")
            append("Pegawai: $employeeName\n")
            append("--------------------------------\n")

            // Layanan utama
            val namaUtama = (serviceName ?: "").take(18).padEnd(18)
            val hargaUtama = currencyFormat.format(servicePrice).padStart(14)
            append("$namaUtama$hargaUtama\n")

            // Layanan tambahan
            if (layananTambahanList.isNotEmpty()) {
                append("\nLayanan Tambahan:\n")
                layananTambahanList.forEachIndexed { index, item ->
                    val nama = "${index + 1}. ${item.nama ?: ""}".take(18).padEnd(18)
                    val harga = currencyFormat.format(item.harga ?: 0).padStart(14)
                    append("$nama$harga\n")
                }
                append("--------------------------------\n")
                val additionalTotal = layananTambahanList.sumOf { it.harga ?: 0 }
                append("Sub Tambahan: ${currencyFormat.format(additionalTotal)}\n")
            }

            append("================================\n")
            append("\u001BE\u0001") // Bold on
            append("TOTAL BAYAR: ${currencyFormat.format(totalPrice)}\n")
            append("\u001BE\u0000") // Bold off
            append("================================\n")
            append("\u001Ba\u0001") // Center alignment
            append("Terima kasih telah memilih\n")
            append("KAARYNS LAUNDRY\n")
            append("Kepuasan Anda prioritas kami\n")
            append("\u001Ba\u0000") // Left alignment
            append("\n\n\n")

            // Cut paper command
            append("\u001Dm\u0000") // Partial cut
        }
    }

    // Fungsi utama untuk print ke Bluetooth dengan retry mechanism
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun printToBluetooth(message: String) {
        Thread {
            try {
                if (bluetoothAdapter == null) {
                    runOnUiThread { showToast("Bluetooth tidak tersedia") }
                    return@Thread
                }

                if (!bluetoothAdapter!!.isEnabled) {
                    runOnUiThread { showToast("Bluetooth tidak aktif") }
                    return@Thread
                }

                if (!hasBluetoothConnectPermission()) {
                    runOnUiThread {
                        showToast("Izin Bluetooth diperlukan untuk mencetak")
                        requestBluetoothPermissions()
                    }
                    return@Thread
                }

                val pairedDevices = try {
                    bluetoothAdapter!!.bondedDevices
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException accessing bonded devices", e)
                    runOnUiThread {
                        showToast("Izin Bluetooth tidak cukup untuk mengakses perangkat")
                        requestBluetoothPermissions()
                    }
                    return@Thread
                }

                var printerDevice: BluetoothDevice? = null
                for (device in pairedDevices) {
                    try {
                        if (device.address == printerMAC) {
                            printerDevice = device
                            break
                        }
                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException accessing device info", e)
                        continue
                    }
                }

                if (printerDevice == null) {
                    runOnUiThread { showToast("Printer tidak ditemukan. MAC: $printerMAC") }
                    return@Thread
                }

                // Coba beberapa metode koneksi
                var connected = false
                var attempts = 0
                val maxAttempts = 3

                while (!connected && attempts < maxAttempts) {
                    attempts++
                    try {
                        // Tutup koneksi sebelumnya jika ada
                        try {
                            bluetoothSocket?.close()
                        } catch (e: Exception) {
                            // Ignore
                        }

                        // Method 1: Standard RFCOMM
                        if (attempts == 1) {
                            Log.d(TAG, "Attempting standard RFCOMM connection (attempt $attempts)")
                            bluetoothSocket = printerDevice.createRfcommSocketToServiceRecord(printerUUID)
                        }
                        // Method 2: Insecure RFCOMM
                        else if (attempts == 2) {
                            Log.d(TAG, "Attempting insecure RFCOMM connection (attempt $attempts)")
                            bluetoothSocket = printerDevice.createInsecureRfcommSocketToServiceRecord(printerUUID)
                        }
                        // Method 3: Reflection method (backup)
                        else {
                            Log.d(TAG, "Attempting reflection method connection (attempt $attempts)")
                            val method = printerDevice.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                            bluetoothSocket = method.invoke(printerDevice, 1) as BluetoothSocket
                        }

                        // Cancel discovery jika masih berjalan
                        if (bluetoothAdapter!!.isDiscovering) {
                            bluetoothAdapter!!.cancelDiscovery()
                        }

                        // Connect dengan timeout
                        bluetoothSocket?.connect()

                        // Test koneksi
                        if (bluetoothSocket?.isConnected == true) {
                            connected = true
                            Log.d(TAG, "Bluetooth connection successful on attempt $attempts")
                        }

                    } catch (e: SecurityException) {
                        Log.e(TAG, "SecurityException during connection attempt $attempts", e)
                        if (attempts >= maxAttempts) {
                            runOnUiThread {
                                showToast("Bluetooth permission error: ${e.message}")
                                requestBluetoothPermissions()
                            }
                            return@Thread
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Connection attempt $attempts failed", e)
                        if (attempts >= maxAttempts) {
                            runOnUiThread { showToast("Gagal koneksi ke printer: ${e.message}") }
                            return@Thread
                        }
                        // Wait before retry
                        Thread.sleep(1000)
                    }
                }

                if (!connected) {
                    runOnUiThread { showToast("Gagal menghubungkan ke printer setelah $maxAttempts percobaan") }
                    return@Thread
                }

                // Send data
                try {
                    outputStream = bluetoothSocket?.outputStream
                    if (outputStream != null) {
                        // ESC/POS commands untuk thermal printer
                        val initPrinter = byteArrayOf(0x1B, 0x40) // ESC @
                        val cutPaper = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // GS V B

                        outputStream!!.write(initPrinter)
                        outputStream!!.write(message.toByteArray(Charsets.UTF_8))
                        outputStream!!.write(cutPaper)
                        outputStream!!.flush()

                        // Wait for printing to complete
                        Thread.sleep(2000)

                        runOnUiThread { showToast("Invoice berhasil dicetak!") }
                        Log.d(TAG, "Print successful")
                    } else {
                        runOnUiThread { showToast("Output stream null") }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error during printing", e)
                    runOnUiThread { showToast("Error saat mencetak: ${e.message}") }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in printToBluetooth", e)
                runOnUiThread { showToast("Error tidak terduga: ${e.message}") }
            } finally {
                // Cleanup
                try {
                    outputStream?.close()
                    bluetoothSocket?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing Bluetooth connection", e)
                }
            }
        }.start()
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "Bluetooth permissions granted")
                    showToast("Izin Bluetooth diberikan")
                } else {
                    showToast("Izin Bluetooth ditolak. Fungsi cetak tidak dapat digunakan.")
                    Log.w(TAG, "Bluetooth permissions denied")
                }
            }
        }
    }

    // Cleanup di onDestroy
    override fun onDestroy() {
        super.onDestroy()
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Bluetooth connection in onDestroy", e)
        }
    }

    // Utility function untuk Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}