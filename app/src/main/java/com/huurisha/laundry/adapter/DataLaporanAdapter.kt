package com.huurisha.laundry.adapter

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatalaporan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listLaporan[position]

        holder.tvnomor.text = "${position + 1}." // Nomor urut
        holder.tvNama.text = item.namapelangganlaporan ?: "Tidak diketahui"
        holder.namalayanan.text = "Layanan: ${item.namalayananlaporan ?: "Tidak diketahui"}"
        holder.tambahlayanan.text = item.tambahlaporan ?: "Tidak ada layanan tambahan"
        holder.harga.text = item.totalbayar ?: "Rp 0"

        // Tampilkan tanggal dari "terdafter"
        val tanggal = item.terdafter ?: "Tidak diketahui"
        holder.terdaftar.text = "Tanggal: ${formatTanggal(tanggal)}"

        // Set status pembayaran dan button berdasarkan kondisi
        setupStatusAndButton(holder, item, position)

        holder.buttonstatus.setOnClickListener {
            handleButtonClick(holder, item, position)
        }
    }

    private fun setupStatusAndButton(holder: ViewHolder, item: ModelLaporan, position: Int) {
        when (item.statuspembayaran) {
            "Belum dibayar" -> {
                // Status: Belum dibayar - warna merah
                holder.statuspembayaram.text = "Belum Dibayar"
                holder.statuspembayaram.setTextColor(Color.RED)

                // Button: Bayar Sekarang - warna merah
                holder.buttonstatus.text = "Bayar Sekarang"
                holder.buttonstatus.setBackgroundColor(Color.RED)
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Hanya untuk status selain "Sudah diambil", sembunyikan TextView tanggal
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
            "Sudah dibayar" -> {
                // Status: Lunas - warna hijau
                holder.statuspembayaram.text = "Sudah Dibayar"
                holder.statuspembayaram.setTextColor(Color.parseColor("#E65100"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#FFF9C4"))

                // Button: Ambil Sekarang - warna biru
                holder.buttonstatus.text = "Ambil Sekarang"
                holder.buttonstatus.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Sembunyikan TextView tanggal
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
            "Sudah diambil" -> {
                // Status: Sudah diambil - warna hijau
                holder.statuspembayaram.text = "Selesai"
                holder.statuspembayaram.setTextColor(Color.parseColor("#4CAF50"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#E8F5E9"))

                // Sembunyikan button
                holder.buttonstatus.visibility = View.GONE

                // Tampilkan tanggal pengambilan di TextView terpisah
                val tanggalPengambilan = item.tanggalPengambilan ?: getCurrentDateTime()
                holder.tvTanggalPengambilan.text = "Diambil: ${formatTanggal(tanggalPengambilan)}"
                holder.tvTanggalPengambilan.setTextColor(Color.parseColor("#666666")) // Abu-abu
                holder.tvTanggalPengambilan.visibility = View.VISIBLE
            }
            else -> {
                // Default untuk status lainnya (Sudah dibayar via metode lain)
                holder.statuspembayaram.text = "Sudah Dibayar"
                holder.statuspembayaram.setTextColor(Color.parseColor("#4CAF50"))
                holder.statuspembayaram.setBackgroundColor(Color.parseColor("#E8F5E9"))

                holder.buttonstatus.text = "Ambil Sekarang"
                holder.buttonstatus.setBackgroundColor(Color.parseColor("#2196F3")) // Blue
                holder.buttonstatus.setTextColor(Color.WHITE)
                holder.buttonstatus.visibility = View.VISIBLE
                holder.buttonstatus.isEnabled = true

                // Sembunyikan TextView tanggal
                holder.tvTanggalPengambilan.visibility = View.GONE
            }
        }
    }

    private fun handleButtonClick(holder: ViewHolder, item: ModelLaporan, position: Int) {
        val context = holder.itemView.context

        when (item.statuspembayaran) {
            "Belum dibayar" -> {
                // Ubah status menjadi sudah dibayar
                updateStatusPembayaran(item, "Sudah dibayar", holder, position)
                Toast.makeText(context, "Pembayaran berhasil dikonfirmasi", Toast.LENGTH_SHORT).show()

                // Callback ke Activity
                onActionCallback?.invoke(item, "PAYMENT_CONFIRMED")
            }
            "Sudah dibayar" -> {
                // Ubah status menjadi sudah diambil
                val tanggalPengambilan = getCurrentDateTime()
                updateStatusPembayaran(item, "Sudah diambil", holder, position, tanggalPengambilan)
                Toast.makeText(context, "Pesanan berhasil diambil", Toast.LENGTH_SHORT).show()

                // Callback ke Activity
                onActionCallback?.invoke(item, "ORDER_PICKED_UP")
            }
            "Sudah diambil" -> {
                // Tidak perlu action karena button sudah tersembunyi
                Toast.makeText(context, "Pesanan sudah diambil", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStatusPembayaran(
        item: ModelLaporan,
        newStatus: String,
        holder: ViewHolder,
        position: Int,
        tanggalPengambilan: String? = null
    ) {
        val database = FirebaseDatabase.getInstance().getReference("laporan")

        // Pastikan transactionId tidak null
        val transactionId = item.transactionId ?: generateTransactionId()

        if (item.transactionId == null) {
            item.transactionId = transactionId
        }

        // Update item di list lokal DULU
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
                when (newStatus) {
                    "Sudah dibayar" -> item.statuspembayaran = "Belum dibayar"
                    "Sudah diambil" -> item.statuspembayaran = "Sudah dibayar"
                }

                Toast.makeText(
                    holder.itemView.context,
                    "Gagal mengupdate status: ${exception.message}",
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
        val tvTanggalPengambilan = itemView.findViewById<TextView>(R.id.tvTanggalPengambilan) // TextView baru
    }
}