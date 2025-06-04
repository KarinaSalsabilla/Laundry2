package com.huurisha.laundry

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.OutputStream
import java.util.*

class BluetoothPrinterManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    // UUID standar untuk Serial Port Profile (SPP)
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    // Periksa apakah Bluetooth tersedia
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null
    }

    // Periksa apakah Bluetooth aktif - DENGAN PERMISSION CHECK
    fun isBluetoothEnabled(): Boolean {
        return try {
            if (!hasBluetoothPermissions()) {
                false
            } else {
                bluetoothAdapter?.isEnabled == true
            }
        } catch (e: SecurityException) {
            false
        }
    }

    // Periksa permission Bluetooth - IMPROVED VERSION
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ memerlukan BLUETOOTH_CONNECT permission
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 11 ke bawah
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

    // Periksa permission untuk scanning (Android 12+)
    private fun hasBluetoothScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Untuk Android < 12, tidak perlu permission khusus untuk scan
            true
        }
    }

    // Dapatkan daftar perangkat yang sudah dipasangkan - DENGAN PROPER PERMISSION CHECK
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            if (!isBluetoothEnabled() || !hasBluetoothPermissions()) {
                emptyList()
            } else {
                bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
            }
        } catch (e: SecurityException) {
            // Log error jika diperlukan
            emptyList()
        }
    }

    // Koneksi ke printer - DENGAN IMPROVED ERROR HANDLING
    fun connectToPrinter(device: BluetoothDevice, callback: (Boolean, String) -> Unit) {
        if (!hasBluetoothPermissions()) {
            callback(false, "Permission Bluetooth tidak diberikan")
            return
        }

        if (!isBluetoothEnabled()) {
            callback(false, "Bluetooth tidak aktif")
            return
        }

        try {
            // Tutup koneksi sebelumnya jika ada
            disconnect()

            bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream

            // Cek apakah koneksi benar-benar berhasil
            if (bluetoothSocket?.isConnected == true && outputStream != null) {
                callback(true, "Berhasil terhubung ke ${getDeviceName(device)}")
            } else {
                callback(false, "Koneksi gagal - tidak dapat membuat stream output")
                disconnect()
            }
        } catch (e: IOException) {
            callback(false, "Gagal terhubung: ${e.message}")
            disconnect()
        } catch (e: SecurityException) {
            callback(false, "Akses Bluetooth ditolak: ${e.message}")
            disconnect()
        } catch (e: Exception) {
            callback(false, "Error tidak terduga: ${e.message}")
            disconnect()
        }
    }

    // Helper function untuk mendapatkan nama device dengan safe permission check
    private fun getDeviceName(device: BluetoothDevice): String {
        return try {
            if (hasBluetoothPermissions()) {
                device.name ?: "Unknown Device"
            } else {
                "Unknown Device"
            }
        } catch (e: SecurityException) {
            "Unknown Device"
        }
    }

    // Disconnect dari printer - IMPROVED VERSION
    fun disconnect() {
        try {
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            // Log error jika diperlukan, tapi jangan crash
            e.printStackTrace()
        } catch (e: Exception) {
            // Handle unexpected errors
            e.printStackTrace()
        } finally {
            outputStream = null
            bluetoothSocket = null
        }
    }

    // Kirim data ke printer - DENGAN BETTER ERROR HANDLING
    fun printText(text: String): Boolean {
        return try {
            val stream = outputStream
            if (stream != null && isConnected()) {
                stream.write(text.toByteArray(Charsets.UTF_8))
                stream.flush()
                true
            } else {
                false
            }
        } catch (e: IOException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    // Kirim perintah ESC/POS - DENGAN BETTER ERROR HANDLING
    fun sendCommand(command: ByteArray): Boolean {
        return try {
            val stream = outputStream
            if (stream != null && isConnected()) {
                stream.write(command)
                stream.flush()
                true
            } else {
                false
            }
        } catch (e: IOException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    // Periksa apakah terhubung - SAFER VERSION
    fun isConnected(): Boolean {
        return try {
            bluetoothSocket?.isConnected == true && outputStream != null
        } catch (e: Exception) {
            false
        }
    }

    // Method untuk mengecek apakah permission sudah diberikan
    fun checkPermissions(): Boolean {
        return hasBluetoothPermissions()
    }

    // Method untuk mengecek scan permission (berguna untuk discovery)
    fun checkScanPermission(): Boolean {
        return hasBluetoothScanPermission()
    }

    // Method untuk mendapatkan daftar permission yang dibutuhkan
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

    // Method untuk mendapatkan permission yang diperlukan untuk connect saja
    fun getConnectPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
    }
}

// ESC/POS Commands Helper - TIDAK BERUBAH
object ESCPOSCommands {
    // Perintah dasar
    val INIT = byteArrayOf(0x1B, 0x40) // Initialize printer
    val LF = byteArrayOf(0x0A) // Line feed
    val CR = byteArrayOf(0x0D) // Carriage return
    val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x42, 0x00) // Cut paper

    // Text alignment
    val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)

    // Text size
    val TEXT_NORMAL = byteArrayOf(0x1B, 0x21, 0x00)
    val TEXT_DOUBLE_WIDTH = byteArrayOf(0x1B, 0x21, 0x20)
    val TEXT_DOUBLE_HEIGHT = byteArrayOf(0x1B, 0x21, 0x10)
    val TEXT_DOUBLE_SIZE = byteArrayOf(0x1B, 0x21, 0x30)

    // Text style
    val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    val UNDERLINE_ON = byteArrayOf(0x1B, 0x2D, 0x01)
    val UNDERLINE_OFF = byteArrayOf(0x1B, 0x2D, 0x00)

    // Fungsi helper untuk membuat separator line
    fun createSeparatorLine(char: String = "-", length: Int = 32): String {
        return char.repeat(length) + "\n"
    }
}