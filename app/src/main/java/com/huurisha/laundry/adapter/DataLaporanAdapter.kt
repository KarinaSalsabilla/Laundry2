package com.huurisha.laundry.adapter

import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelLaporan
import java.text.SimpleDateFormat
import java.util.*

class DataLaporanAdapter(
    private val listLaporan: ArrayList<ModelLaporan>,
    private val onActionCallback: ((ModelLaporan, String) -> Unit)? = null
) : RecyclerView.Adapter<DataLaporanAdapter.ViewHolder>() {

    companion object {
        const val STATUS_UNPAID = "Belum dibayar"
        const val STATUS_PAID = "Sudah dibayar"
        const val STATUS_COMPLETED = "Sudah diambil"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatalaporan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLaporan[position]
        val context = holder.itemView.context

        holder.tvnomor.text = "${position + 1}." // Nomor urut
        holder.tvNama.text = item.namapelangganlaporan ?: context.getString(R.string.unknown)
        holder.namalayanan.text = "${context.getString(R.string.service)}: ${item.namalayananlaporan ?: context.getString(R.string.unknown)}"

        // Handle additional services dengan format count
        setupAdditionalServices(holder, item, context)

        holder.harga.text = item.totalbayar ?: "Rp 0"

        // Tampilkan tanggal dari "terdafter"
        val tanggal = item.terdafter ?: context.getString(R.string.unknown)
        holder.terdaftar.text = "${context.getString(R.string.date)}: ${formatTanggal(tanggal)}"

        // Set status pembayaran dan button berdasarkan kondisi
        setupStatusAndButton(holder, item, position)

        holder.buttonstatus.setOnClickListener {
            handleButtonClick(holder, item, position)
        }
    }

    private fun setupAdditionalServices(holder: ViewHolder, item: ModelLaporan, context: android.content.Context) {
        val additionalServices = item.tambahlaporan

        if (additionalServices.isNullOrEmpty() || additionalServices.trim().isEmpty()) {
            // Tidak ada layanan tambahan
            holder.tambahlayanan.text = context.getString(R.string.no_additional_service)
            holder.tambahlayanan.setTextColor(Color.GRAY)
            return
        }

        // Parse layanan tambahan - asumsi dipisahkan dengan delimiter (comma, semicolon, dll)
        val servicesList = parseAdditionalServices(additionalServices)
        val serviceCount = servicesList.size

        when {
            serviceCount == 0 -> {
                holder.tambahlayanan.text = context.getString(R.string.no_additional_service)
                holder.tambahlayanan.setTextColor(Color.GRAY)
            }
            serviceCount == 1 -> {
                // Tampilkan langsung jika hanya 1 layanan
                holder.tambahlayanan.text = servicesList[0]
                holder.tambahlayanan.setTextColor(Color.parseColor("#2196F3"))
            }
            else -> {
                // Tampilkan format "+X layanan tambahan"
                val countText = if (serviceCount == 1) {
                    context.getString(R.string.additional_services_count_single, serviceCount)
                } else {
                    context.getString(R.string.additional_services_count_plural, serviceCount)
                }

                holder.tambahlayanan.text = countText
                holder.tambahlayanan.setTextColor(Color.parseColor("#FF9800")) // Orange color

                // Set click listener untuk toggle detail
                setupAdditionalServicesClickListener(holder, servicesList, countText, context)
            }
        }
    }

    private fun setupAdditionalServicesClickListener(
        holder: ViewHolder,
        servicesList: List<String>,
        countText: String,
        context: android.content.Context
    ) {
        var isExpanded = false

        holder.tambahlayanan.setOnClickListener {
            if (isExpanded) {
                // Sembunyikan detail, tampilkan count
                holder.tambahlayanan.text = countText
                holder.tambahlayanan.setTextColor(Color.parseColor("#FF9800"))
                isExpanded = false
            } else {
                // Tampilkan detail
                val detailText = servicesList.joinToString("\n• ", "• ")
                holder.tambahlayanan.text = detailText
                holder.tambahlayanan.setTextColor(Color.parseColor("#4CAF50"))
                isExpanded = true
            }
        }
    }

    private fun parseAdditionalServices(additionalServices: String): List<String> {
        if (additionalServices.isBlank()) return emptyList()

        // Coba berbagai delimiter yang mungkin digunakan
        val delimiters = arrayOf(",", ";", "|", "\n", "•", "-")

        for (delimiter in delimiters) {
            if (additionalServices.contains(delimiter)) {
                return additionalServices.split(delimiter)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
        }

        // Jika tidak ada delimiter yang cocok, anggap sebagai satu layanan
        return listOf(additionalServices.trim())
    }

    private fun setupStatusAndButton(holder: ViewHolder, item: ModelLaporan, position: Int) {
        val context = holder.itemView.context

        // Dapatkan string resource untuk perbandingan
        val statusUnpaidResource = context.getString(R.string.status_unpaid)
        val statusPaidResource = context.getString(R.string.status_paid)
        val statusCompletedResource = context.getString(R.string.status_completed)

        // Normalisasi status untuk pengecekan yang lebih robust
        val currentStatus = item.statuspembayaran?.trim() ?: ""

        when {
            currentStatus.equals(STATUS_UNPAID, ignoreCase = true) ||
                    currentStatus.equals(statusUnpaidResource, ignoreCase = true) ||
                    currentStatus.equals("Pay Later", ignoreCase = true) -> {
                // Status: Belum dibayar - warna merah
                holder.statuspembayaram.text = statusUnpaidResource
                holder.statuspembayaram.setTextColor(Color.RED)
                holder.statuspembayaram.setBackgroundColor(Color.TRANSPARENT)

                // Button: Bayar Sekarang - warna merah
                holder.buttonstatus.text = context.getString(R.string.btn_pay_now)
                holder.buttonstatus.setBackgroundColor(Color.RED)
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Sembunyikan TextView tanggal pengambilan
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
            currentStatus.equals(STATUS_PAID, ignoreCase = true) ||
                    currentStatus.equals(statusPaidResource, ignoreCase = true) -> {
                // Status: Sudah dibayar - warna orange
                holder.statuspembayaram.text = statusPaidResource
                holder.statuspembayaram.setTextColor(Color.parseColor("#E65100"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#FFF9C4"))

                // Button: Ambil Sekarang - warna biru
                holder.buttonstatus.text = context.getString(R.string.btn_pick_up_now)
                holder.buttonstatus.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Sembunyikan TextView tanggal pengambilan
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
            currentStatus.equals(STATUS_COMPLETED, ignoreCase = true) ||
                    currentStatus.equals(statusCompletedResource, ignoreCase = true) -> {
                // Status: Sudah diambil - warna hijau
                holder.statuspembayaram.text = statusCompletedResource
                holder.statuspembayaram.setTextColor(Color.parseColor("#4CAF50"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#E8F5E9"))

                // Sembunyikan button
                holder.buttonstatus.visibility = View.GONE

                // Tampilkan tanggal pengambilan di TextView terpisah
                val tanggalPengambilan = item.tanggalPengambilan ?: getCurrentDateTime()
                holder.tvTanggalPengambilan.text = "${context.getString(R.string.picked_up_on)}: ${formatTanggal(tanggalPengambilan)}"
                holder.tvTanggalPengambilan.setTextColor(Color.parseColor("#666666")) // Abu-abu
                holder.tvTanggalPengambilan.visibility = View.VISIBLE
            }
            else -> {
                // Default untuk status lainnya (dianggap sudah dibayar)
                holder.statuspembayaram.text = statusPaidResource
                holder.statuspembayaram.setTextColor(Color.parseColor("#4CAF50"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#E8F5E9"))

                holder.buttonstatus.text = context.getString(R.string.btn_pick_up_now)
                holder.buttonstatus.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Sembunyikan TextView tanggal pengambilan
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
        }
    }

    private fun handleButtonClick(holder: ViewHolder, item: ModelLaporan, position: Int) {
        val context = holder.itemView.context

        // Dapatkan string resource untuk perbandingan
        val statusUnpaidResource = context.getString(R.string.status_unpaid)
        val statusPaidResource = context.getString(R.string.status_paid)
        val statusCompletedResource = context.getString(R.string.status_completed)

        // Normalisasi status untuk pengecekan yang lebih robust
        val currentStatus = item.statuspembayaran?.trim() ?: ""

        when {
            currentStatus.equals(STATUS_UNPAID, ignoreCase = true) ||
                    currentStatus.equals(statusUnpaidResource, ignoreCase = true) ||
                    currentStatus.equals("Pay Later", ignoreCase = true) -> {
                // Tampilkan dialog pembayaran
                showPaymentDialog(context, item, holder, position)
            }
            currentStatus.equals(STATUS_PAID, ignoreCase = true) ||
                    currentStatus.equals(statusPaidResource, ignoreCase = true) -> {
                // Ubah status menjadi sudah diambil
                val tanggalPengambilan = getCurrentDateTime()
                updateStatusPembayaran(item, STATUS_COMPLETED, holder, position, tanggalPengambilan)
                Toast.makeText(context, context.getString(R.string.toast_order_picked_up_success), Toast.LENGTH_SHORT).show()

                // Callback ke Activity
                onActionCallback?.invoke(item, "ORDER_PICKED_UP")
            }
            currentStatus.equals(STATUS_COMPLETED, ignoreCase = true) ||
                    currentStatus.equals(statusCompletedResource, ignoreCase = true) -> {
                // Tidak perlu action karena button sudah tersembunyi
                Toast.makeText(context, context.getString(R.string.toast_order_already_picked_up), Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Default: dianggap sudah dibayar, bisa diambil
                val tanggalPengambilan = getCurrentDateTime()
                updateStatusPembayaran(item, STATUS_COMPLETED, holder, position, tanggalPengambilan)
                Toast.makeText(context, context.getString(R.string.toast_order_picked_up_success), Toast.LENGTH_SHORT).show()

                // Callback ke Activity
                onActionCallback?.invoke(item, "ORDER_PICKED_UP")
            }
        }
    }

    private fun showPaymentDialog(context: android.content.Context, item: ModelLaporan, holder: ViewHolder, position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.activity_bayar_nanti)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(true)

        // Ambil referensi ke button-button dalam dialog
        val btnTunai = dialog.findViewById<Button>(R.id.btnTunaiLaporan)
        val btnQRIS = dialog.findViewById<Button>(R.id.btnQRISLaporan)
        val btnDana = dialog.findViewById<Button>(R.id.btnDanaLaporan)
        val btnGopay = dialog.findViewById<Button>(R.id.btnGopayLaporan)
        val btnOvo = dialog.findViewById<Button>(R.id.btnOvoLaporan)
        val tvBatal = dialog.findViewById<TextView>(R.id.tvBatalDialogLaporan)

        // Set text dengan strings resource
        btnTunai?.text = context.getString(R.string.payment_cash)
        btnQRIS?.text = context.getString(R.string.payment_qris)
        btnDana?.text = context.getString(R.string.payment_dana)
        btnGopay?.text = context.getString(R.string.payment_gopay)
        btnOvo?.text = context.getString(R.string.payment_ovo)
        tvBatal?.text = context.getString(R.string.btn_cancel)

        // Set click listener untuk setiap metode pembayaran
        btnTunai?.setOnClickListener {
            processPayment(item, "Tunai", holder, position, dialog)
        }

        btnQRIS?.setOnClickListener {
            processPayment(item, "QRIS", holder, position, dialog)
        }

        btnDana?.setOnClickListener {
            processPayment(item, "DANA", holder, position, dialog)
        }

        btnGopay?.setOnClickListener {
            processPayment(item, "GoPay", holder, position, dialog)
        }

        btnOvo?.setOnClickListener {
            processPayment(item, "OVO", holder, position, dialog)
        }

        // Set click listener untuk tombol batal
        tvBatal?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun processPayment(item: ModelLaporan, metodePembayaran: String, holder: ViewHolder, position: Int, dialog: Dialog) {
        val context = holder.itemView.context

        // Update metode pembayaran
        item.metodepembayaran = metodePembayaran

        // Ubah status menjadi sudah dibayar
        updateStatusPembayaran(item, STATUS_PAID, holder, position)

        Toast.makeText(
            context,
            context.getString(R.string.toast_payment_success, metodePembayaran),
            Toast.LENGTH_SHORT
        ).show()

        // Callback ke Activity
        onActionCallback?.invoke(item, "PAYMENT_CONFIRMED")

        // Tutup dialog
        dialog.dismiss()
    }

    private fun updateStatusPembayaran(
        item: ModelLaporan,
        newStatus: String,
        holder: ViewHolder,
        position: Int,
        tanggalPengambilan: String? = null
    ) {
        val database = FirebaseDatabase.getInstance().getReference("laporan")
        val context = holder.itemView.context

        // Pastikan transactionId tidak null
        val transactionId = item.transactionId ?: generateTransactionId()

        if (item.transactionId == null) {
            item.transactionId = transactionId
        }

        // Update item di list lokal DULU
        val oldStatus = item.statuspembayaran
        item.statuspembayaran = newStatus
        if (tanggalPengambilan != null) {
            item.tanggalPengambilan = tanggalPengambilan
        }

        // Siapkan data lengkap untuk update ke Firebase
        val updates = hashMapOf<String, Any>(
            "namapelangganlaporan" to (item.namapelangganlaporan ?: ""),
            "terdafter" to (item.terdafter ?: getCurrentDateTime()),
            "namalayananlaporan" to (item.namalayananlaporan ?: ""),
            "tambahlaporan" to (item.tambahlaporan ?: ""),
            "totalbayar" to (item.totalbayar ?: "Rp 0"),
            "metodepembayaran" to (item.metodepembayaran ?: ""),
            "statuspembayaran" to newStatus,
            "transactionId" to transactionId
        )

        // Tambahkan tanggal pengambilan jika ada
        if (tanggalPengambilan != null) {
            updates["tanggalPengambilan"] = tanggalPengambilan
        }

        // Update di Firebase
        database.child(transactionId).updateChildren(updates)
            .addOnSuccessListener {
                // Update UI setelah berhasil update database
                setupStatusAndButton(holder, item, position)

                // Notify adapter bahwa item telah berubah
                notifyItemChanged(position)
            }
            .addOnFailureListener { exception ->
                // Jika gagal, kembalikan status ke semula
                item.statuspembayaran = oldStatus

                Toast.makeText(
                    context,
                    context.getString(R.string.toast_update_status_failed, exception.message),
                    Toast.LENGTH_SHORT
                ).show()

                // Update UI kembali
                setupStatusAndButton(holder, item, position)
                notifyItemChanged(position)
            }
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatTanggal(tanggal: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(tanggal)
            date?.let { outputFormat.format(it) } ?: tanggal
        } catch (e: Exception) {
            tanggal
        }
    }

    private fun formatTanggalPendek(tanggal: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val date = inputFormat.parse(tanggal)
            date?.let { outputFormat.format(it) } ?: tanggal
        } catch (e: Exception) {
            tanggal
        }
    }

    private fun generateTransactionId(): String {
        return "-O${System.currentTimeMillis()}"
    }

    override fun getItemCount(): Int = listLaporan.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardlaporan)
        val tvnomor = itemView.findViewById<TextView>(R.id.no)
        val terdaftar = itemView.findViewById<TextView>(R.id.daftar)
        val tvNama = itemView.findViewById<TextView>(R.id.namalapor)
        val namalayanan = itemView.findViewById<TextView>(R.id.laporan)
        val tambahlayanan = itemView.findViewById<TextView>(R.id.tambahlapor)
        val harga = itemView.findViewById<TextView>(R.id.hargalaporan)
        val statuspembayaram = itemView.findViewById<TextView>(R.id.tvStatusPembayaran)
        val buttonstatus = itemView.findViewById<Button>(R.id.btnStatusPembayaran)
        val tvTanggalPengambilan = itemView.findViewById<TextView>(R.id.tvTanggalPengambilan)
    }
}