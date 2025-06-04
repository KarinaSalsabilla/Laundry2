package com.huurisha.laundry.adapter

import android.Manifest
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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.pegawai.tambah_pegawai

class DataPegawaiAdapter(private val listPegawai: ArrayList<ModelPegawai>) :
    RecyclerView.Adapter<DataPegawaiAdapter.ViewHolder>() {

    private lateinit var appContext: Context
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("pegawai")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapegawai, parent, false)
        appContext = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listPegawai[position]

        holder.tvId.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = "Alamat= ${item.alamatPegawai}"
        holder.tvNoHP.text = "No Hp= ${item.noHpPegawai}"
        holder.cabangPegawai.text = "Cabang= ${item.idCabangPegawai}"
        holder.terdaftar.text = "Terdaftar= ${item.terdaftar}"

        holder.btHubungi.setOnClickListener {
            showCustomContactDialog(item.noHpPegawai, item.namaPegawai)
        }

        holder.btLihat.setOnClickListener {
            showPegawaiDialog(item, position)
        }

        holder.cvCard.setOnClickListener {
            val intent = Intent(appContext, tambah_pegawai::class.java)
            intent.putExtra("judul", "Edit Pegawai")
            intent.putExtra("idPegawai", item.idPegawai)
            intent.putExtra("namaPegawai", item.namaPegawai)
            intent.putExtra("noHpPegawai", item.noHpPegawai)
            intent.putExtra("alamatPegawai", item.alamatPegawai)
            intent.putExtra("idCabangPegawai", item.idCabangPegawai)
            appContext.startActivity(intent)
        }
    }

    private fun showCustomContactDialog(phoneNumber: String?, customerName: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(appContext, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = Dialog(appContext)
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_hubungi, null)
        dialog.setContentView(dialogView)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        val jdlNama = dialogView.findViewById<TextView>(R.id.jdlnama)
        val btnTelepon = dialogView.findViewById<Button>(R.id.btntelepon)
        val btnWhatsApp = dialogView.findViewById<Button>(R.id.btnwa)

        jdlNama.text = customerName ?: "Pegawai"

        btnTelepon.setOnClickListener {
            dialog.dismiss()
            callPhoneNumber(phoneNumber)
        }

        btnWhatsApp.setOnClickListener {
            dialog.dismiss()
            openWhatsApp(phoneNumber, customerName)
        }

        dialog.show()
    }

    private fun callPhoneNumber(phoneNumber: String) {
        try {
            val cleanNumber = cleanPhoneNumber(phoneNumber)
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$cleanNumber")

            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
                appContext.startActivity(intent)
            } else {
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
            val whatsappNumber = formatWhatsAppNumber(cleanNumber)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}")

            val packageManager = appContext.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                appContext.startActivity(intent)
            } else {
                Toast.makeText(appContext, "WhatsApp tidak terinstall, membuka di browser", Toast.LENGTH_SHORT).show()
                appContext.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(appContext, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
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

    private fun showPegawaiDialog(pegawai: ModelPegawai, position: Int) {
        val dialog = Dialog(appContext)
        val dialogView = LayoutInflater.from(appContext).inflate(R.layout.dialog_mod_pegawai, null)
        dialog.setContentView(dialogView)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        val tvId = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_ID)
        val tvNama = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_Nama)
        val tvAlamat = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_ALAMAT)
        val tvNoHP = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_NOHP)
        val tvCabang = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_CABANG)
        val tvTerdaftar = dialogView.findViewById<TextView>(R.id.tvDIALOG_MOD_PEGAWAI_DAFTAR)
        val btEdit = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEGAWAI_Edit)
        val btHapus = dialogView.findViewById<Button>(R.id.btDIALOG_MOD_PEGAWAI_Hapus)

        tvId.text = pegawai.idPegawai
        tvNama.text = pegawai.namaPegawai
        tvAlamat.text = pegawai.alamatPegawai
        tvNoHP.text = pegawai.noHpPegawai
        tvCabang.text = pegawai.idCabangPegawai
        tvTerdaftar.text = pegawai.terdaftar

        btEdit.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(appContext, tambah_pegawai::class.java)
            intent.putExtra("judul", "Edit Pegawai")
            intent.putExtra("idPegawai", pegawai.idPegawai)
            intent.putExtra("namaPegawai", pegawai.namaPegawai)
            intent.putExtra("noHpPegawai", pegawai.noHpPegawai)
            intent.putExtra("alamatPegawai", pegawai.alamatPegawai)
            intent.putExtra("idCabangPegawai", pegawai.idCabangPegawai)
            appContext.startActivity(intent)
        }

        btHapus.setOnClickListener {
            dialog.dismiss()
            // Gunakan dialog konfirmasi yang sama seperti DataCabangAdapter
            showDeleteConfirmationDialog(pegawai, position)
        }

        dialog.show()
    }

    // Dialog konfirmasi hapus - sama seperti di DataCabangAdapter
    private fun showDeleteConfirmationDialog(pegawai: ModelPegawai, position: Int) {
        val alertDialog = AlertDialog.Builder(appContext)
            .setTitle("Konfirmasi Hapus")
            .setMessage("Apakah Anda yakin ingin menghapus pegawai \"${pegawai.namaPegawai}\"?\n\nData yang dihapus tidak dapat dikembalikan.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Ya, Hapus") { dialog, _ ->
                dialog.dismiss()
                deletePegawaiFromDatabase(pegawai, position)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        alertDialog.show()

        // Opsional: Ubah warna tombol untuk menekankan aksi berbahaya
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(appContext, android.R.color.holo_red_dark)
        )
    }

    // Fungsi untuk menghapus pegawai dari database - diperbaiki seperti DataCabangAdapter
    private fun deletePegawaiFromDatabase(pegawai: ModelPegawai, position: Int) {
        val pegawaiId = pegawai.idPegawai

        if (pegawaiId.isNullOrEmpty()) {
            Toast.makeText(appContext, "ID pegawai tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan loading toast
        Toast.makeText(appContext, "Menghapus pegawai...", Toast.LENGTH_SHORT).show()

        databaseReference.child(pegawaiId).removeValue()
            .addOnSuccessListener {
                // Hapus dari list lokal dan update RecyclerView
                if (position < listPegawai.size) {
                    listPegawai.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, listPegawai.size)
                }

                Toast.makeText(
                    appContext,
                    "Pegawai \"${pegawai.namaPegawai}\" berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    appContext,
                    "Gagal menghapus pegawai: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun getItemCount(): Int {
        return listPegawai.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cvCard: View = itemView.findViewById(R.id.carddatapegawai)
        val tvId: TextView = itemView.findViewById(R.id.idpegawai)
        val tvNama: TextView = itemView.findViewById(R.id.namapega)
        val tvAlamat: TextView = itemView.findViewById(R.id.alamatpegawai)
        val tvNoHP: TextView = itemView.findViewById(R.id.hppegawai)
        val cabangPegawai: TextView = itemView.findViewById(R.id.cabangpegawai)
        val terdaftar: TextView = itemView.findViewById(R.id.daftar)
        val btHubungi: Button = itemView.findViewById(R.id.hubungi)
        val btLihat: Button = itemView.findViewById(R.id.lihat)
    }
}