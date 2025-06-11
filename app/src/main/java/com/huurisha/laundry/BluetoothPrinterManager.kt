package com.huurisha.laundry

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
import java.util.UUID

class BluetoothPrinterManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null
    private var connectionJob: Job? = null

    // Multiple UUID untuk kompatibilitas lebih baik
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val ALTERNATIVE_UUID = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB")
    private val THIRD_UUID = UUID.fromString("00001103-0000-1000-8000-00805F9B34FB")

    init {
        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error initializing BluetoothAdapter", e)
        }
    }

    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error checking Bluetooth status", e)
            false
        }
    }

    fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            val bluetoothPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED

            val bluetoothAdminPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED

            bluetoothPermission && bluetoothAdminPermission
        }
    }

    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            if (!checkPermissions()) {
                android.util.Log.w("BluetoothPrinter", "Missing Bluetooth permissions")
                emptyList()
            } else {
                bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            }
        } catch (e: SecurityException) {
            android.util.Log.e("BluetoothPrinter", "Security exception getting paired devices", e)
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error getting paired devices", e)
            emptyList()
        }
    }

    // IMPROVED CONNECTION METHOD dengan better error handling
    // Method untuk connect menggunakan MAC address
    fun connectToPrinter(macAddress: String, callback: (Boolean, String) -> Unit) {
        if (!checkPermissions()) {
            callback(false, "Permission Bluetooth tidak diberikan")
            return
        }

        if (!isBluetoothEnabled()) {
            callback(false, "Bluetooth tidak aktif")
            return
        }

        // Validasi format MAC address
        if (!isValidMacAddress(macAddress)) {
            callback(false, "Format MAC address tidak valid: $macAddress")
            return
        }

        try {
            val device = bluetoothAdapter?.getRemoteDevice(macAddress)
            device?.let {
                connectToPrinter(it, callback)
            } ?: callback(false, "Tidak dapat membuat device dari MAC address: $macAddress")
        } catch (e: IllegalArgumentException) {
            callback(false, "MAC address tidak valid: $macAddress - ${e.message}")
        } catch (e: Exception) {
            callback(false, "Error saat membuat device dari MAC: ${e.message}")
        }
    }

    // Overload method untuk tetap support BluetoothDevice
    fun connectToPrinter(device: BluetoothDevice, callback: (Boolean, String) -> Unit) {
        if (!checkPermissions()) {
            callback(false, "Permission Bluetooth tidak diberikan")
            return
        }

        if (!isBluetoothEnabled()) {
            callback(false, "Bluetooth tidak aktif")
            return
        }

        // Cancel koneksi sebelumnya jika ada
        connectionJob?.cancel()

        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tutup koneksi sebelumnya dengan delay
                disconnect()
                delay(2000) // Wait longer untuk memastikan socket benar-benar tertutup

                // Hentikan discovery untuk menghindari interferensi
                try {
                    if (checkPermissions()) {
                        bluetoothAdapter?.cancelDiscovery()
                        delay(1000) // Wait untuk discovery berhenti
                    }
                } catch (e: SecurityException) {
                    android.util.Log.w("BluetoothPrinter", "Permission denied for cancelDiscovery", e)
                }

                android.util.Log.d("BluetoothPrinter", "Attempting connection to ${getDeviceName(device)}")

                var connected = false
                var lastError = ""
                val connectionMethods = listOf(
                    "Standard Socket",
                    "Reflection Method 1",
                    "Reflection Method 2",
                    "Alternative UUID",
                    "Third UUID"
                )

                for ((index, methodName) in connectionMethods.withIndex()) {
                    if (connected) break

                    try {
                        android.util.Log.d("BluetoothPrinter", "Trying $methodName...")

                        when (index) {
                            0 -> { // Standard socket
                                if (checkPermissions()) {
                                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                                }
                            }
                            1 -> { // Reflection method 1
                                if (checkPermissions()) {
                                    val method: Method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                                    bluetoothSocket = method.invoke(device, 1) as BluetoothSocket
                                }
                            }
                            2 -> { // Reflection method 2 - different channel
                                if (checkPermissions()) {
                                    val method: Method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                                    bluetoothSocket = method.invoke(device, 4) as BluetoothSocket
                                }
                            }
                            3 -> { // Alternative UUID
                                if (checkPermissions()) {
                                    bluetoothSocket = device.createRfcommSocketToServiceRecord(ALTERNATIVE_UUID)
                                }
                            }
                            4 -> { // Third UUID
                                if (checkPermissions()) {
                                    bluetoothSocket = device.createRfcommSocketToServiceRecord(THIRD_UUID)
                                }
                            }
                        }

                        // Attempt connection dengan timeout handling
                        bluetoothSocket?.let { socket ->
                            try {
                                // Set socket properties jika memungkinkan
                                socket.connect()

                                // Verify connection
                                if (socket.isConnected) {
                                    outputStream = socket.outputStream
                                    inputStream = socket.inputStream

                                    // Test dengan mengirim data sederhana
                                    val testSuccess = withContext(Dispatchers.IO) {
                                        try {
                                            outputStream?.write(ESCPOSCommands.INIT)
                                            outputStream?.flush()
                                            delay(500) // Wait for response
                                            true
                                        } catch (e: Exception) {
                                            android.util.Log.w("BluetoothPrinter", "Test write failed: ${e.message}")
                                            false
                                        }
                                    }

                                    if (testSuccess) {
                                        connected = true
                                        android.util.Log.d("BluetoothPrinter", "$methodName connected successfully")
                                    } else {
                                        throw IOException("Test write failed - printer may not be responding")
                                    }
                                } else {
                                    throw IOException("Socket not connected after connect() call")
                                }
                            } catch (e: IOException) {
                                lastError = "$methodName: ${e.message}"
                                android.util.Log.w("BluetoothPrinter", "$methodName failed: ${e.message}")
                                safeCloseSocket()
                                delay(1000) // Wait before next attempt
                            }
                        }

                    } catch (e: SecurityException) {
                        lastError = "$methodName: Security exception - ${e.message}"
                        android.util.Log.w("BluetoothPrinter", lastError)
                        safeCloseSocket()
                    } catch (e: Exception) {
                        lastError = "$methodName: ${e.message}"
                        android.util.Log.w("BluetoothPrinter", lastError)
                        safeCloseSocket()
                        delay(1000) // Wait before next attempt
                    }
                }

                // Callback dengan hasil
                withContext(Dispatchers.Main) {
                    if (connected) {
                        callback(true, "Berhasil terhubung ke ${getDeviceName(device)}")
                    } else {
                        callback(false, "Semua metode koneksi gagal. Error: $lastError")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    android.util.Log.e("BluetoothPrinter", "Unexpected error during connection", e)
                    callback(false, "Error tidak terduga: ${e.message}")
                    safeCloseSocket()
                }
            }
        }
    }

    // Safe socket closing method
    private fun safeCloseSocket() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: Exception) {
            android.util.Log.w("BluetoothPrinter", "Error closing socket safely", e)
        } finally {
            inputStream = null
            outputStream = null
            bluetoothSocket = null
        }
    }

    // Method untuk koneksi dengan timeout menggunakan MAC address
    fun connectWithTimeout(macAddress: String, timeoutMs: Long = 15000, callback: (Boolean, String) -> Unit) {
        val timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMs)
            connectionJob?.let { job ->
                if (job.isActive) {
                    job.cancel()
                    disconnect()
                    callback(false, "Koneksi timeout setelah ${timeoutMs/1000} detik")
                }
            }
        }

        connectToPrinter(macAddress) { success, message ->
            timeoutJob.cancel() // Cancel timeout jika koneksi berhasil/gagal
            callback(success, message)
        }
    }

    // Overload untuk BluetoothDevice
    fun connectWithTimeout(device: BluetoothDevice, timeoutMs: Long = 15000, callback: (Boolean, String) -> Unit) {
        val timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMs)
            connectionJob?.let { job ->
                if (job.isActive) {
                    job.cancel()
                    disconnect()
                    callback(false, "Koneksi timeout setelah ${timeoutMs/1000} detik")
                }
            }
        }

        connectToPrinter(device) { success, message ->
            timeoutJob.cancel() // Cancel timeout jika koneksi berhasil/gagal
            callback(success, message)
        }
    }

    // Utility method untuk validasi MAC address
    private fun isValidMacAddress(macAddress: String): Boolean {
        return try {
            val macPattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$"
            macAddress.matches(macPattern.toRegex())
        } catch (e: Exception) {
            false
        }
    }

    // Method untuk mendapatkan device dari MAC address
    fun getDeviceFromMac(macAddress: String): BluetoothDevice? {
        return try {
            if (!checkPermissions()) {
                android.util.Log.w("BluetoothPrinter", "Missing permissions to get device")
                return null
            }

            if (!isValidMacAddress(macAddress)) {
                android.util.Log.w("BluetoothPrinter", "Invalid MAC address format: $macAddress")
                return null
            }

            bluetoothAdapter?.getRemoteDevice(macAddress)
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("BluetoothPrinter", "Invalid MAC address: $macAddress", e)
            null
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error getting device from MAC", e)
            null
        }
    }

    // Method untuk mencari device berdasarkan nama atau MAC
    fun findDevice(identifier: String): BluetoothDevice? {
        return try {
            if (!checkPermissions()) {
                android.util.Log.w("BluetoothPrinter", "Missing permissions to find device")
                return null
            }

            val pairedDevices = getPairedDevices()

            // Cari berdasarkan MAC address terlebih dahulu
            if (isValidMacAddress(identifier)) {
                return pairedDevices.find { it.address.equals(identifier, ignoreCase = true) }
                    ?: bluetoothAdapter?.getRemoteDevice(identifier)
            }

            // Jika bukan MAC address, cari berdasarkan nama
            pairedDevices.find { device ->
                try {
                    device.name?.contains(identifier, ignoreCase = true) == true
                } catch (e: SecurityException) {
                    false
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error finding device", e)
            null
        }
    }

    // Method untuk mendapatkan list device dengan info lengkap
    fun getPairedDevicesWithInfo(): List<DeviceInfo> {
        return try {
            if (!checkPermissions()) {
                android.util.Log.w("BluetoothPrinter", "Missing permissions to get device info")
                emptyList()
            } else {
                getPairedDevices().map { device ->
                    DeviceInfo(
                        device = device,
                        name = try {
                            device.name ?: "Unknown Device"
                        } catch (e: SecurityException) {
                            "Unknown Device"
                        },
                        address = device.address,
                        bondState = device.bondState,
                        type = device.type
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error getting paired devices info", e)
            emptyList()
        }
    }

    // Data class untuk informasi device
    data class DeviceInfo(
        val device: BluetoothDevice,
        val name: String,
        val address: String,
        val bondState: Int,
        val type: Int
    ) {
        fun getTypeString(): String {
            return when (type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
                BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual Mode"
                else -> "Unknown"
            }
        }

        fun getBondStateString(): String {
            return when (bondState) {
                BluetoothDevice.BOND_BONDED -> "Paired"
                BluetoothDevice.BOND_BONDING -> "Pairing"
                BluetoothDevice.BOND_NONE -> "Not Paired"
                else -> "Unknown"
            }
        }
    }
    private fun getDeviceName(device: BluetoothDevice): String {
        return try {
            if (checkPermissions()) {
                device.name ?: device.address ?: "Unknown Device"
            } else {
                device.address ?: "Unknown Device"
            }
        } catch (e: SecurityException) {
            android.util.Log.w("BluetoothPrinter", "Security exception getting device name", e)
            device.address ?: "Unknown Device"
        } catch (e: Exception) {
            android.util.Log.w("BluetoothPrinter", "Error getting device name", e)
            "Unknown Device"
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        safeCloseSocket()
        android.util.Log.d("BluetoothPrinter", "Disconnected from printer")
    }

    // IMPROVED PRINT METHOD dengan connection check
    fun printText(text: String, maxRetries: Int = 2): Boolean {
        if (!isConnected()) {
            android.util.Log.w("BluetoothPrinter", "Cannot print - not connected")
            return false
        }

        var attempt = 0
        while (attempt < maxRetries) {
            try {
                val stream = outputStream
                if (stream != null) {
                    stream.write(text.toByteArray(Charsets.UTF_8))
                    stream.flush()
                    android.util.Log.d("BluetoothPrinter", "Text sent successfully on attempt ${attempt + 1}")
                    return true
                }
            } catch (e: IOException) {
                android.util.Log.e("BluetoothPrinter", "Error printing text (attempt ${attempt + 1}): ${e.message}")
                // Jika IOException, kemungkinan koneksi terputus
                if (e.message?.contains("socket", true) == true ||
                    e.message?.contains("closed", true) == true) {
                    android.util.Log.w("BluetoothPrinter", "Socket appears to be closed, stopping retries")
                    break
                }
            } catch (e: Exception) {
                android.util.Log.e("BluetoothPrinter", "Unexpected error printing text (attempt ${attempt + 1}): ${e.message}")
            }

            attempt++
            if (attempt < maxRetries) {
                Thread.sleep(1000) // Wait before retry
            }
        }
        return false
    }

    // IMPROVED SEND COMMAND dengan better error handling
    fun sendCommand(command: ByteArray, maxRetries: Int = 2): Boolean {
        if (!isConnected()) {
            android.util.Log.w("BluetoothPrinter", "Cannot send command - not connected")
            return false
        }

        var attempt = 0
        while (attempt < maxRetries) {
            try {
                val stream = outputStream
                if (stream != null) {
                    stream.write(command)
                    stream.flush()
                    android.util.Log.d("BluetoothPrinter", "Command sent successfully on attempt ${attempt + 1}")
                    return true
                }
            } catch (e: IOException) {
                android.util.Log.e("BluetoothPrinter", "Error sending command (attempt ${attempt + 1}): ${e.message}")
                // Jika IOException, kemungkinan koneksi terputus
                if (e.message?.contains("socket", true) == true ||
                    e.message?.contains("closed", true) == true) {
                    android.util.Log.w("BluetoothPrinter", "Socket appears to be closed, stopping retries")
                    break
                }
            } catch (e: Exception) {
                android.util.Log.e("BluetoothPrinter", "Unexpected error sending command (attempt ${attempt + 1}): ${e.message}")
            }

            attempt++
            if (attempt < maxRetries) {
                Thread.sleep(500) // Wait before retry
            }
        }
        return false
    }

    fun isConnected(): Boolean {
        return try {
            val socket = bluetoothSocket
            val stream = outputStream
            socket?.isConnected == true && stream != null
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error checking connection", e)
            false
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }

    // Method helper untuk test koneksi yang lebih reliable
    fun testConnection(): Boolean {
        return try {
            if (isConnected()) {
                // Test dengan command sederhana dan tunggu response
                val success = sendCommand(ESCPOSCommands.INIT)
                if (success) {
                    Thread.sleep(200) // Give printer time to process
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("BluetoothPrinter", "Error testing connection", e)
            false
        }
    }

    // Method untuk mendapatkan device info
    fun getDeviceInfo(device: BluetoothDevice): String {
        return try {
            if (checkPermissions()) {
                "Name: ${device.name ?: "Unknown"}, Address: ${device.address}, Type: ${device.type}"
            } else {
                "Address: ${device.address} (Name requires permission)"
            }
        } catch (e: Exception) {
            "Error getting device info: ${e.message}"
        }
    }

    // ESC/POS Commands
    object ESCPOSCommands {
        val INIT = byteArrayOf(0x1B, 0x40)
        val LF = byteArrayOf(0x0A)
        val CR = byteArrayOf(0x0D)
        val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x42, 0x00)
        val FEED_LINE = byteArrayOf(0x1B, 0x64, 0x02) // Feed 2 lines

        val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
        val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
        val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)

        val TEXT_NORMAL = byteArrayOf(0x1B, 0x21, 0x00)
        val TEXT_DOUBLE_WIDTH = byteArrayOf(0x1B, 0x21, 0x20)
        val TEXT_DOUBLE_HEIGHT = byteArrayOf(0x1B, 0x21, 0x10)
        val TEXT_DOUBLE_SIZE = byteArrayOf(0x1B, 0x21, 0x30)

        val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
        val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
        val UNDERLINE_ON = byteArrayOf(0x1B, 0x2D, 0x01)
        val UNDERLINE_OFF = byteArrayOf(0x1B, 0x2D, 0x00)

        fun createSeparatorLine(char: String = "-", length: Int = 32): String {
            return char.repeat(length) + "\n"
        }
    }
}