package com.huurisha.laundry.adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.pegawai.tambah_pegawai
import com.huurisha.laundry.pelanggan.tambahPelanggan
import android.Manifest
import android.app.AlertDialog


class DataPelangganAdapter (private val ListPelanggan: ArrayList<ModelPelanggan>) : RecyclerView.Adapter<DataPelangganAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapelanggan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListPelanggan[position]
        holder.tvid.text = item.idPelanggan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = "Alamat= ${item.alamatPelanggan}"
        holder.tvNoHP.text = "No Hp= ${item.noHpPelanggan}"
        holder.cabangpelang.text = "Cabang= ${item.idCabang}"
        holder.terdafter.text = "Terdaftar= ${item.terdafter}"

        holder.btHubungi.setOnClickListener {
            // Tampilkan custom dialog dengan layout XML yang Anda buat
            showCustomContactDialog(item.noHpPelanggan, item.namaPelanggan)
        }

        holder.btLihat.setOnClickListener{
            showPelangganDialog(item,position)
        }

        holder.cvCard.setOnClickListener {
            val intent = Intent(appContext, tambahPelanggan::class.java)
            intent.putExtra("judul","Edit Pelanggan")
            intent.putExtra("idPelanggan",item.idPelanggan)
            intent.putExtra("namaPelanggan",item.namaPelanggan)
            intent.putExtra("noHpPelanggan",item.noHpPelanggan)
            intent.putExtra("alamatPelanggan",item.alamatPelanggan)
            intent.putExtra("idCabang",item.idCabang)
            appContext.startActivity(intent)
        }
    }

    // Fungsi baru untuk menampilkan custom dialog dengan layout XML
    private fun showCustomContactDialog(phoneNumber: String?, customerName: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(appContext, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat dialog custom
        val dialog = Dialog(appContext)
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_hubungi, null)
        dialog.setContentView(dialogView)

        // Set dialog properties
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        // Find views dalam dialog
        val jdlNama = dialogView.findViewById<TextView>(R.id.jdlnama)
        val btnTelepon = dialogView.findViewById<Button>(R.id.btntelepon)
        val btnWhatsApp = dialogView.findViewById<Button>(R.id.btnwa)

        // Set nama pelanggan di dialog
        jdlNama.text = customerName ?: "Pelanggan"

        // Set click listeners untuk tombol
        btnTelepon.setOnClickListener {
            dialog.dismiss()
            callPhoneNumber(phoneNumber)
        }

        btnWhatsApp.setOnClickListener {
            dialog.dismiss()
            openWhatsApp(phoneNumber, customerName)
        }

        // Tampilkan dialog
        dialog.show()
    }

    // Fungsi alternatif menggunakan AlertDialog (jika ingin tetap menggunakan yang lama)
    private fun showContactOptionsDialog(phoneNumber: String?, customerName: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(appContext, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf("Telepon", "WhatsApp")

        val dialog = androidx.appcompat.app.AlertDialog.Builder(appContext)
            .setTitle("Hubungi $customerName")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> callPhoneNumber(phoneNumber) // Telepon
                    1 -> openWhatsApp(phoneNumber, customerName) // WhatsApp
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun callPhoneNumber(phoneNumber: String) {
        try {
            val cleanNumber = cleanPhoneNumber(phoneNumber)
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$cleanNumber")

            // Cek permission untuk melakukan panggilan
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
                appContext.startActivity(intent)
            } else {
                // Jika tidak ada permission, buka dialer saja
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$cleanNumber")
                appContext.startActivity(dialIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(appContext, "Tidak dapat membuka aplikasi telepon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(phoneNumber: String?, customerName: String?) {
        try {
            val cleanNumber = cleanPhoneNumber(phoneNumber ?: "")
            val message = "Halo $customerName, ada yang bisa kami bantu?"

            // Format nomor untuk WhatsApp (Indonesia: +62)
            val whatsappNumber = formatWhatsAppNumber(cleanNumber)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}")

            // Cek apakah WhatsApp terinstall
            val packageManager = appContext.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                appContext.startActivity(intent)
            } else {
                // Jika WhatsApp tidak terinstall, buka di browser
                Toast.makeText(appContext, "WhatsApp tidak terinstall, membuka di browser", Toast.LENGTH_SHORT).show()
                appContext.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(appContext, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cleanPhoneNumber(phoneNumber: String): String {
        // Hapus karakter non-digit dan spasi
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }

    private fun formatWhatsAppNumber(phoneNumber: String): String {
        var cleanNumber = cleanPhoneNumber(phoneNumber)

        // Jika dimulai dengan 0, ganti dengan 62 (untuk Indonesia)
        if (cleanNumber.startsWith("0")) {
            cleanNumber = "62" + cleanNumber.substring(1)
        }
        // Jika dimulai dengan +62, hapus tanda +
        else if (cleanNumber.startsWith("+62")) {
            cleanNumber = cleanNumber.substring(1)
        }
        // Jika tidak dimulai dengan 62, tambahkan 62 di depan (asumsi Indonesia)
        else if (!cleanNumber.startsWith("62")) {
            cleanNumber = "62$cleanNumber"
        }

        return cleanNumber
    }

    private fun showPelangganDialog(pelanggan: ModelPelanggan,position: Int) {
        val dialog = Dialog(appContext)
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_pelanggan, null)
        dialog.setContentView(dialogView)

        // Set dialog properties
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        // Find views in dialog
        val tvId = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_ID)
        val tvNama = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_Nama)
        val tvAlamat = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_ALAMAT)
        val tvNoHP = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_NOHP)
        val tvCabang = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_CABANG)
        val tvTerdaftar = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PELANGGAN_DAFTAR)
        val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Edit)
        val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PELANGGAN_Hapus)

        // Set data to dialog views
        tvId.text = pelanggan.idPelanggan
        tvNama.text = pelanggan.namaPelanggan
        tvAlamat.text = pelanggan.alamatPelanggan
        tvNoHP.text = pelanggan.noHpPelanggan
        tvCabang.text = pelanggan.idCabang
        tvTerdaftar.text = pelanggan.terdafter

        // Set button click listeners
        btEdit.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(appContext, tambahPelanggan::class.java)
            intent.putExtra("judul","Edit Pelanggan")
            intent.putExtra("idPelanggan", pelanggan.idPelanggan)
            intent.putExtra("namaPelanggan", pelanggan.namaPelanggan)
            intent.putExtra("noHpPelanggan", pelanggan.noHpPelanggan)
            intent.putExtra("alamatPelanggan", pelanggan.alamatPelanggan)
            intent.putExtra("idCabang", pelanggan.idCabang)
            appContext.startActivity(intent)
        }

        btHapus.setOnClickListener {
            showDeleteConfirmation(pelanggan, dialog)
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(pelanggan: ModelPelanggan, parentDialog: Dialog) {
        val confirmDialog = AlertDialog.Builder(appContext)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus pelanggan ${pelanggan.namaPelanggan}?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Ya") { _, _ ->
                deletePelanggan(pelanggan)
                parentDialog.dismiss()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        confirmDialog.show()
    }

    private fun deletePelanggan(pelanggan: ModelPelanggan) {
        databaseReference = FirebaseDatabase.getInstance().getReference("pelanggan")

        databaseReference.child(pelanggan.idPelanggan!!).removeValue()
            .addOnSuccessListener {
                val position = ListPelanggan.indexOf(pelanggan)
                if (position != -1) {
                    ListPelanggan.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(appContext, "Pelanggan berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(appContext, "Gagal menghapus pelanggan: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return ListPelanggan.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardpelanggan)
        val tvid = itemView.findViewById<TextView>(R.id.idpelanggan)
        val tvNama = itemView.findViewById<TextView>(R.id.namapel)
        val tvAlamat = itemView.findViewById<TextView>(R.id.alamatpel)
        val tvNoHP = itemView.findViewById<TextView>(R.id.hppel)
        val cabangpelang = itemView.findViewById<TextView>(R.id.cabangpel)
        val terdafter = itemView.findViewById<TextView>(R.id.terdaftar)
        val btHubungi = itemView.findViewById<Button>(R.id.hubungipel)
        val btLihat = itemView.findViewById<Button>(R.id.lihatpel)
    }
}