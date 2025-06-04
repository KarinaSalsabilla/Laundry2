//package com.huurisha.laundry
//
//import android.Manifest
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.*
//import android.graphics.pdf.PdfDocument
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Environment
//import android.print.PrintAttributes
//import android.print.PrintDocumentAdapter
//import android.print.PrintDocumentInfo
//import android.print.PrintJob
//import android.print.PrintManager
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.firebase.database.FirebaseDatabase
//import com.huurisha.laundry.adapter.KonfirmasiDataAdapter
//import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
//import com.huurisha.laundry.modeldata.ModelInvoice
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.text.SimpleDateFormat
//import java.util.*
//
//// TAMBAHAN IMPORT UNTUK BLUETOOTH
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothSocket
//
//class Invoice: AppCompatActivity() {
//
//    // Firebase Database
//    val database = FirebaseDatabase.getInstance()
//    val myRef = database.getReference("invoice")
//
//    // Sesuaikan dengan ID di layout XML
//    private lateinit var tvTransactionId: TextView
//    private lateinit var tvDateTime: TextView
//    private lateinit var tvCustomerName: TextView
//    private lateinit var tvEmployeeName: TextView
//    private lateinit var tvServiceName: TextView
//    private lateinit var tvServicePrice: TextView
//    private lateinit var tvAdditionalSubtotal: TextView
//    private lateinit var tvTotal: TextView
//    private lateinit var rvTambahan: RecyclerView
//    private lateinit var btnWhatsApp: Button
//    private lateinit var btnPrint: Button
//
//    // Root view untuk capture
//    private lateinit var invoiceLayout: View
//
//    // TAMBAHAN VARIABLE UNTUK BLUETOOTH PRINTER
//    private lateinit var bluetoothPrinter: BluetoothPrinterManager
//    private var selectedPrinter: BluetoothDevice? = null
//
//    // Permission request launcher
//    private val permissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val allGranted = permissions.all { it.value }
//        if (allGranted) {
//            showPrinterSelection()
//        } else {
//            Toast.makeText(this, "Permission Bluetooth diperlukan untuk mencetak", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private var layananTambahanList = arrayListOf<ModelTransaksiTambahan>()
//    private var customerName: String? = null
//    private var customerPhone: String? = null
//    private var employeeName: String? = null
//    private var transactionId: String? = null
//    private var totalPrice: Int = 0
//    private var servicePrice: Int = 0
//    private var serviceName: String? = null
//    private var currentDateTime: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_invoice)
//
//        // TAMBAHAN: Initialize bluetooth printer
//        bluetoothPrinter = BluetoothPrinterManager(this)
//
//        initViews()
//        loadDataFromIntent()
//        setupRecyclerView()
//        setupButtons()
//
//        // Simpan invoice ke database
//        saveInvoiceToDatabase()
//    }
//
//    private fun initViews() {
//        // Mapping sesuai dengan ID di XML layout
//        tvTransactionId = findViewById(R.id.transaksiInvoice)
//        tvDateTime = findViewById(R.id.tanggaljam)
//        tvCustomerName = findViewById(R.id.namaPelanggan)
//        tvEmployeeName = findViewById(R.id.namaPegawai)
//        tvServiceName = findViewById(R.id.namaLayanan)
//        tvServicePrice = findViewById(R.id.hargaLayanan)
//        tvAdditionalSubtotal = findViewById(R.id.totalTambahan)
//        tvTotal = findViewById(R.id.totalBayar)
//        rvTambahan = findViewById(R.id.rvDatalkonfirmasi)
//        btnWhatsApp = findViewById(R.id.btnwa)
//        btnPrint = findViewById(R.id.btnCetak)
//
//        // Get root layout untuk capture
//        invoiceLayout = findViewById(android.R.id.content)
//    }
//
//    private fun loadDataFromIntent() {
//        // Ambil data dari intent
//        customerName = intent.getStringExtra("customer_name")
//        customerPhone = intent.getStringExtra("customer_phone")
//        employeeName = intent.getStringExtra("employee_name")
//        serviceName = intent.getStringExtra("service_name")
//        servicePrice = intent.getIntExtra("service_price", 0)
//        totalPrice = intent.getIntExtra("total_price", 0)
//        transactionId = intent.getStringExtra("transaction_id")
//
//        val tambahanNamaList = intent.getStringArrayListExtra("tambahan_nama")
//        val tambahanHargaList = intent.getIntegerArrayListExtra("tambahan_harga")
//
//        // Set data ke TextView sesuai layout
//        tvTransactionId.text = transactionId ?: "-O-DEFAULT"
//        tvCustomerName.text = customerName ?: "Customer Name"
//        tvEmployeeName.text = employeeName ?: "Default Employee"
//        tvServiceName.text = serviceName ?: "Layanan Default"
//        tvServicePrice.text = "Rp ${String.format("%,d", servicePrice)}"
//
//        // Set tanggal dan jam saat ini dengan format yang lebih lengkap
//        currentDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
//        tvDateTime.text = currentDateTime
//
//        // Siapkan data layanan tambahan
//        if (tambahanNamaList != null && tambahanHargaList != null) {
//            for (i in tambahanNamaList.indices) {
//                val item = ModelTransaksiTambahan(
//                    nama = tambahanNamaList[i],
//                    harga = tambahanHargaList[i]
//                )
//                layananTambahanList.add(item)
//            }
//        }
//
//        // Hitung subtotal tambahan
//        val additionalSubtotal = layananTambahanList.sumOf { it.harga ?: 0 }
//        tvAdditionalSubtotal.text = "Rp ${String.format("%,d", additionalSubtotal)}"
//        tvTotal.text = "Rp ${String.format("%,d", totalPrice)}"
//    }
//
//    private fun setupRecyclerView() {
//        rvTambahan.layoutManager = LinearLayoutManager(this)
//        rvTambahan.adapter = KonfirmasiDataAdapter(layananTambahanList)
//    }
//
//    private fun setupButtons() {
//        btnWhatsApp.setOnClickListener {
//            sendWhatsAppMessage()
//        }
//
//        btnPrint.setOnClickListener {
//            handlePrintClick()
//        }
//    }
//
//    // FUNGSI BARU: Handle print click dengan permission check
//    private fun handlePrintClick() {
//        if (!bluetoothPrinter.isBluetoothAvailable()) {
//            Toast.makeText(this, "Bluetooth tidak tersedia", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (!bluetoothPrinter.isBluetoothEnabled()) {
//            Toast.makeText(this, "Silakan aktifkan Bluetooth terlebih dahulu", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Check permissions
//        if (bluetoothPrinter.checkPermissions()) {
//            // Permission sudah ada, langsung tampilkan printer selection
//            if (selectedPrinter != null && bluetoothPrinter.isConnected()) {
//                printToBluetoothPrinter()
//            } else {
//                showPrinterSelection()
//            }
//        } else {
//            // Request permission
//            requestBluetoothPermissions()
//        }
//    }
//
//    // FUNGSI BARU: Request Bluetooth permissions
//    private fun requestBluetoothPermissions() {
//        val requiredPermissions = bluetoothPrinter.getRequiredPermissions()
//
//        // Check which permissions are missing
//        val missingPermissions = requiredPermissions.filter { permission ->
//            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
//        }
//
//        if (missingPermissions.isNotEmpty()) {
//            // Show explanation if needed
//            val shouldShowRationale = missingPermissions.any { permission ->
//                ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
//            }
//
//            if (shouldShowRationale) {
//                androidx.appcompat.app.AlertDialog.Builder(this)
//                    .setTitle("Permission Diperlukan")
//                    .setMessage("Aplikasi memerlukan akses Bluetooth untuk mencetak invoice ke printer Bluetooth.")
//                    .setPositiveButton("OK") { _, _ ->
//                        permissionLauncher.launch(missingPermissions.toTypedArray())
//                    }
//                    .setNegativeButton("Batal", null)
//                    .show()
//            } else {
//                permissionLauncher.launch(missingPermissions.toTypedArray())
//            }
//        }
//    }
//
//    private fun generateCustomId(): String {
//        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
//        val random = Random()
//        val result = StringBuilder()
//
//        // Mulai dengan -O (bukan hanya -)
//        result.append("-O")
//
//        // Generate 18 karakter random (karena sudah ada -O di depan)
//        repeat(20) {
//            result.append(chars[random.nextInt(chars.length)])
//        }
//
//        return result.toString()
//    }
//
//    // Fungsi baru untuk menyimpan invoice ke database
//    private fun saveInvoiceToDatabase() {
//        val invoiceId = generateCustomId() // atau tetap pakai myRef.push().key
//
//        val invoice = ModelInvoice(
//            idInvoice = invoiceId,
//            idTransaksi = transactionId,
//            namaPelanggan = customerName,
//            noHpPelanggan = customerPhone,
//            namaPegawai = employeeName,
//            namaLayanan = serviceName,
//            hargaLayanan = servicePrice,
//            layananTambahan = layananTambahanList,
//            totalTambahan = layananTambahanList.sumOf { it.harga ?: 0 },
//            totalBayar = totalPrice,
//            tanggalWaktu = currentDateTime,
//            timestamp = System.currentTimeMillis()
//        )
//
//        // Uncomment jika ingin menyimpan ke Firebase
//        /*
//        myRef.child(invoiceId).setValue(invoice)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Invoice berhasil disimpan dengan ID: $invoiceId", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { exception ->
//                Toast.makeText(this, "Gagal menyimpan invoice: ${exception.message}", Toast.LENGTH_SHORT).show()
//            }
//        */
//    }
//
//    private fun sendWhatsAppMessage() {
//        if (customerPhone.isNullOrEmpty()) {
//            Toast.makeText(this, "Nomor telepon customer tidak tersedia", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val message = createInvoiceMessage()
//
//        try {
//            // Format nomor untuk WhatsApp menggunakan fungsi yang sama seperti di DataPelangganAdapter
//            val cleanNumber = cleanPhoneNumber(customerPhone!!)
//            val whatsappNumber = formatWhatsAppNumber(cleanNumber)
//
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}")
//
//            // Cek apakah WhatsApp terinstall
//            val packageManager = packageManager
//            if (intent.resolveActivity(packageManager) != null) {
//                startActivity(intent)
//            } else {
//                // Jika WhatsApp tidak terinstall, buka di browser
//                Toast.makeText(this, "WhatsApp tidak terinstall, membuka di browser", Toast.LENGTH_SHORT).show()
//                startActivity(intent)
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // Tambahkan fungsi helper yang sama seperti di DataPelangganAdapter
//    private fun cleanPhoneNumber(phoneNumber: String): String {
//        // Hapus karakter non-digit dan spasi
//        return phoneNumber.replace(Regex("[^0-9+]"), "")
//    }
//
//    private fun formatWhatsAppNumber(phoneNumber: String): String {
//        var cleanNumber = cleanPhoneNumber(phoneNumber)
//
//        // Jika dimulai dengan 0, ganti dengan 62 (untuk Indonesia)
//        if (cleanNumber.startsWith("0")) {
//            cleanNumber = "62" + cleanNumber.substring(1)
//        }
//        // Jika dimulai dengan +62, hapus tanda +
//        else if (cleanNumber.startsWith("+62")) {
//            cleanNumber = cleanNumber.substring(1)
//        }
//        // Jika tidak dimulai dengan 62, tambahkan 62 di depan (asumsi Indonesia)
//        else if (!cleanNumber.startsWith("62")) {
//            cleanNumber = "62$cleanNumber"
//        }
//
//        return cleanNumber
//    }
//
//    private fun createInvoiceMessage(): String {
//        val stringBuilder = StringBuilder()
//        stringBuilder.append("*KAARYNS LAUNDRY*\n\n")
//        stringBuilder.append("ID Transaksi: $transactionId\n")
//        stringBuilder.append("Tanggal: ${tvDateTime.text}\n")
//        stringBuilder.append("Customer: $customerName\n")
//        stringBuilder.append("Karyawan: $employeeName\n")
//        stringBuilder.append("Layanan: ${tvServiceName.text}\n\n")
//        stringBuilder.append("--- RINCIAN ---\n")
//        stringBuilder.append("Harga Layanan: ${tvServicePrice.text}\n")
//
//        if (layananTambahanList.isNotEmpty()) {
//            stringBuilder.append("\nLayanan Tambahan:\n")
//            for (item in layananTambahanList) {
//                stringBuilder.append("- ${item.nama}: Rp ${String.format("%,d", item.harga ?: 0)}\n")
//            }
//            stringBuilder.append("Subtotal Tambahan: ${tvAdditionalSubtotal.text}\n")
//        }
//
//        stringBuilder.append("\n*TOTAL: ${tvTotal.text}*\n\n")
//        stringBuilder.append("Terima kasih telah menggunakan layanan kami! 🙏")
//
//        return stringBuilder.toString()
//    }
//
//    // Tampilkan dialog untuk memilih printer
//    private fun showPrinterSelection() {
//        val pairedDevices = bluetoothPrinter.getPairedDevices()
//        if (pairedDevices.isEmpty()) {
//            Toast.makeText(this, "Tidak ada printer yang dipasangkan", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Filter hanya perangkat yang kemungkinan printer
//        val printers = pairedDevices.filter { device ->
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return
//            }
//            device.name?.contains("printer", ignoreCase = true) == true ||
//                    device.name?.contains("POS", ignoreCase = true) == true ||
//                    device.name?.contains("receipt", ignoreCase = true) == true ||
//                    device.bluetoothClass?.majorDeviceClass == 1536 // Imaging class
//        }
//
//        val deviceNames = if (printers.isNotEmpty()) {
//            printers.map { "${it.name} (${it.address})" }.toTypedArray()
//        } else {
//            // Jika tidak ada yang terfilter, tampilkan semua
//            pairedDevices.map { "${it.name} (${it.address})" }.toTypedArray()
//        }
//
//        androidx.appcompat.app.AlertDialog.Builder(this)
//            .setTitle("Pilih Printer")
//            .setItems(deviceNames) { _, which ->
//                val selectedDevice = if (printers.isNotEmpty()) printers[which] else pairedDevices[which]
//                connectAndPrint(selectedDevice)
//            }
//            .setNegativeButton("Batal", null)
//            .show()
//    }
//
//    // Koneksi ke printer dan mulai print
//    private fun connectAndPrint(device: BluetoothDevice) {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        Toast.makeText(this, "Menghubungkan ke ${device.name}...", Toast.LENGTH_SHORT).show()
//
//        Thread {
//            bluetoothPrinter.connectToPrinter(device) { success, message ->
//                runOnUiThread {
//                    if (success) {
//                        selectedPrinter = device
//                        printToBluetoothPrinter()
//                    } else {
//                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//        }.start()
//    }
//
//    // Print invoice ke bluetooth printer
//    private fun printToBluetoothPrinter() {
//        if (!bluetoothPrinter.isConnected()) {
//            Toast.makeText(this, "Printer tidak terhubung", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        Thread {
//            try {
//                // Initialize printer
//                bluetoothPrinter.sendCommand(ESCPOSCommands.INIT)
//
//                // Header - Center align
//                bluetoothPrinter.sendCommand(ESCPOSCommands.ALIGN_CENTER)
//                bluetoothPrinter.sendCommand(ESCPOSCommands.TEXT_DOUBLE_SIZE)
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_ON)
//                bluetoothPrinter.printText("KAARYNS LAUNDRY\n")
//                bluetoothPrinter.sendCommand(ESCPOSCommands.TEXT_NORMAL)
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_OFF)
//                bluetoothPrinter.printText("Invoice Pembayaran\n")
//
//                // Separator
//                bluetoothPrinter.sendCommand(ESCPOSCommands.ALIGN_LEFT)
//                bluetoothPrinter.printText(ESCPOSCommands.createSeparatorLine("="))
//
//                // Transaction details
//                bluetoothPrinter.printText("ID Transaksi: $transactionId\n")
//                bluetoothPrinter.printText("Tanggal: $currentDateTime\n")
//                bluetoothPrinter.printText("Customer: $customerName\n")
//                bluetoothPrinter.printText("Karyawan: $employeeName\n")
//
//                bluetoothPrinter.printText(ESCPOSCommands.createSeparatorLine("-"))
//
//                // Service details
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_ON)
//                bluetoothPrinter.printText("RINCIAN LAYANAN:\n")
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_OFF)
//
//                bluetoothPrinter.printText("${serviceName}\n")
//                bluetoothPrinter.printText("Harga: Rp ${String.format("%,d", servicePrice)}\n")
//
//                // Additional services
//                if (layananTambahanList.isNotEmpty()) {
//                    bluetoothPrinter.printText("\nLayanan Tambahan:\n")
//                    for (item in layananTambahanList) {
//                        bluetoothPrinter.printText("- ${item.nama}\n")
//                        bluetoothPrinter.printText("  Rp ${String.format("%,d", item.harga ?: 0)}\n")
//                    }
//
//                    val additionalTotal = layananTambahanList.sumOf { it.harga ?: 0 }
//                    bluetoothPrinter.printText("Subtotal Tambahan: Rp ${String.format("%,d", additionalTotal)}\n")
//                }
//
//                bluetoothPrinter.printText(ESCPOSCommands.createSeparatorLine("="))
//
//                // Total
//                bluetoothPrinter.sendCommand(ESCPOSCommands.TEXT_DOUBLE_WIDTH)
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_ON)
//                bluetoothPrinter.printText("TOTAL: Rp ${String.format("%,d", totalPrice)}\n")
//                bluetoothPrinter.sendCommand(ESCPOSCommands.TEXT_NORMAL)
//                bluetoothPrinter.sendCommand(ESCPOSCommands.BOLD_OFF)
//
//                bluetoothPrinter.printText(ESCPOSCommands.createSeparatorLine("="))
//
//                // Footer
//                bluetoothPrinter.sendCommand(ESCPOSCommands.ALIGN_CENTER)
//                bluetoothPrinter.printText("Terima kasih atas kepercayaan Anda\n")
//                bluetoothPrinter.printText("Semoga puas dengan layanan kami\n\n")
//
//                // Cut paper
//                bluetoothPrinter.sendCommand(ESCPOSCommands.CUT_PAPER)
//
//                runOnUiThread {
//                    Toast.makeText(this, "Invoice berhasil dicetak!", Toast.LENGTH_SHORT).show()
//                }
//
//            } catch (e: Exception) {
//                runOnUiThread {
//                    Toast.makeText(this, "Gagal mencetak: ${e.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//        }.start()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        bluetoothPrinter.disconnect()
//    }
//}