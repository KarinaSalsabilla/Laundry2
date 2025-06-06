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
import com.huurisha.laundry.modeldata.ModelCabang
import com.huurisha.laundry.modeldata.ModelLayanan

class DataCabangAdapter (private val ListCabang: ArrayList<ModelCabang>) : RecyclerView.Adapter<DataCabangAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatacabang, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListCabang[position]
        holder.tvid.text = item.idcabang
        holder.tvNama.text = item.namacabang
        holder.harga.text = "${appContext.getString(R.string.tvalamat)}= ${item.alamatcabang}"
        holder.cabang.text ="${appContext.getString(R.string.tvnohp)} =  ${item.nohp}"
        holder.jamoper.text ="${appContext.getString(R.string.jamoper)} = ${item.jamopera}"

        holder.cvCard.setOnClickListener {

        }

        holder.hubungi.setOnClickListener {
            showCustomContactDialog(item.nohp, item.namacabang)
        }

        holder.hapus.setOnClickListener {
            // Tampilkan dialog konfirmasi sebelum menghapus
            showDeleteConfirmationDialog(item, position)
        }
    }

    // Dialog konfirmasi hapus
    private fun showDeleteConfirmationDialog(item: ModelCabang, position: Int) {
        val alertDialog = AlertDialog.Builder(appContext)
            .setTitle(appContext.getString(R.string.dialog_delete_title))
            .setMessage(appContext.getString(R.string.dialog_delete_message, item.namacabang))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(appContext.getString(R.string.btn_yes_delete)) { dialog, _ ->
                dialog.dismiss()
                deleteCabangFromDatabase(item, position)
            }
            .setNegativeButton(appContext.getString(R.string.btn_cancel)) { dialog, _ ->
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

    // Fungsi untuk menghapus cabang dari database - IMPROVED VERSION
    private fun deleteCabangFromDatabase(item: ModelCabang, position: Int) {
        val idToDelete = item.idcabang

        if (idToDelete.isNullOrEmpty()) {
            Toast.makeText(appContext, appContext.getString(R.string.toast_invalid_branch_id), Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan progress dialog
        val progressDialog = AlertDialog.Builder(appContext)
            .setMessage(appContext.getString(R.string.dialog_deleting_branch))
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Inisialisasi database reference - PERBAIKAN: gunakan "cabang" bukan "DataCabang"
        databaseReference = FirebaseDatabase.getInstance().getReference("cabang")

        databaseReference.child(idToDelete).removeValue()
            .addOnSuccessListener {
                progressDialog.dismiss()

                // Hapus dari list lokal dan update RecyclerView
                if (position >= 0 && position < ListCabang.size) {
                    ListCabang.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, ListCabang.size)
                }

                Toast.makeText(
                    appContext,
                    appContext.getString(R.string.toast_branch_deleted_success, item.namacabang),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()

                Toast.makeText(
                    appContext,
                    appContext.getString(R.string.toast_branch_delete_failed, exception.message),
                    Toast.LENGTH_LONG
                ).show()

                // Log error untuk debugging
                android.util.Log.e("DeleteCabang", "Error deleting cabang: ${exception.message}", exception)
            }
    }

    // Alternative method dengan pengecekan koneksi internet
    private fun deleteCabangFromDatabaseWithConnectionCheck(item: ModelCabang, position: Int) {
        val idToDelete = item.idcabang

        if (idToDelete.isNullOrEmpty()) {
            Toast.makeText(appContext, appContext.getString(R.string.toast_invalid_branch_id), Toast.LENGTH_SHORT).show()
            return
        }

        // Cek koneksi internet
        if (!isNetworkAvailable()) {
            Toast.makeText(appContext, appContext.getString(R.string.toast_no_internet_connection), Toast.LENGTH_SHORT).show()
            return
        }

        // Tampilkan progress dialog
        val progressDialog = AlertDialog.Builder(appContext)
            .setMessage(appContext.getString(R.string.dialog_deleting_branch))
            .setCancelable(false)
            .create()
        progressDialog.show()

        // Inisialisasi database reference - PERBAIKAN: gunakan "cabang" bukan "DataCabang"
        databaseReference = FirebaseDatabase.getInstance().getReference("cabang")

        // Pertama, cek apakah data exist di database
        databaseReference.child(idToDelete).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Data ada, lanjutkan penghapusan
                    databaseReference.child(idToDelete).removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()

                            // Hapus dari list lokal dan update RecyclerView
                            if (position >= 0 && position < ListCabang.size) {
                                ListCabang.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, ListCabang.size)
                            }

                            Toast.makeText(
                                appContext,
                                appContext.getString(R.string.toast_branch_deleted_success, item.namacabang),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { exception ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                appContext,
                                appContext.getString(R.string.toast_branch_delete_failed, exception.message),
                                Toast.LENGTH_LONG
                            ).show()
                            android.util.Log.e("DeleteCabang", "Error deleting: ${exception.message}", exception)
                        }
                } else {
                    progressDialog.dismiss()
                    // Data tidak ada di database, hapus dari list lokal saja
                    if (position >= 0 && position < ListCabang.size) {
                        ListCabang.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, ListCabang.size)
                    }
                    Toast.makeText(appContext, appContext.getString(R.string.toast_data_not_found_in_database), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(
                    appContext,
                    appContext.getString(R.string.toast_database_access_failed, exception.message),
                    Toast.LENGTH_LONG
                ).show()
                android.util.Log.e("CheckCabang", "Error checking data: ${exception.message}", exception)
            }
    }

    // Helper function untuk cek koneksi internet
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    // Improved dialog konfirmasi dengan lebih detail
    private fun showImprovedDeleteConfirmationDialog(item: ModelCabang, position: Int) {
        val message = appContext.getString(
            R.string.dialog_detailed_delete_message,
            item.namacabang,
            item.alamatcabang,
            item.nohp
        )

        val alertDialog = AlertDialog.Builder(appContext)
            .setTitle(appContext.getString(R.string.dialog_warning_delete_title))
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(appContext.getString(R.string.btn_delete_confirm)) { dialog, _ ->
                dialog.dismiss()
                deleteCabangFromDatabase(item, position)
                // Atau gunakan yang dengan connection check:
                // deleteCabangFromDatabaseWithConnectionCheck(item, position)
            }
            .setNegativeButton(appContext.getString(R.string.btn_cancel_action)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        alertDialog.show()

        // Ubah warna tombol untuk menekankan aksi berbahaya
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
            setTextColor(ContextCompat.getColor(appContext, android.R.color.holo_red_dark))
            setBackgroundColor(ContextCompat.getColor(appContext, android.R.color.transparent))
        }
    }

    private fun showCustomContactDialog(phoneNumber: String?, customerName: String?) {
        if (phoneNumber.isNullOrEmpty()) {
            Toast.makeText(appContext, appContext.getString(R.string.toast_phone_not_available), Toast.LENGTH_SHORT).show()
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
        jdlNama.text = customerName ?: appContext.getString(R.string.default_branch_name)

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
            Toast.makeText(appContext, appContext.getString(R.string.toast_phone_not_available), Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf(
            appContext.getString(R.string.option_phone_call),
            appContext.getString(R.string.option_whatsapp)
        )

        val dialog = androidx.appcompat.app.AlertDialog.Builder(appContext)
            .setTitle(appContext.getString(R.string.dialog_contact_title, customerName))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> callPhoneNumber(phoneNumber) // Telepon
                    1 -> openWhatsApp(phoneNumber, customerName) // WhatsApp
                }
            }
            .setNegativeButton(appContext.getString(R.string.btn_cancel)) { dialog, _ ->
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
            Toast.makeText(appContext, appContext.getString(R.string.toast_cannot_open_phone_app), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWhatsApp(phoneNumber: String?, customerName: String?) {
        try {
            val cleanNumber = cleanPhoneNumber(phoneNumber ?: "")
            val message = appContext.getString(R.string.whatsapp_default_message, customerName)

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
                Toast.makeText(appContext, appContext.getString(R.string.toast_whatsapp_not_installed), Toast.LENGTH_SHORT).show()
                appContext.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(appContext, appContext.getString(R.string.toast_cannot_open_whatsapp), Toast.LENGTH_SHORT).show()
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

    override fun getItemCount(): Int {
        return ListCabang.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardcabang)
        val tvid = itemView.findViewById<TextView>(R.id.idcabang)
        val tvNama = itemView.findViewById<TextView>(R.id.namapel)
        val harga = itemView.findViewById<TextView>(R.id.alamatpel)
        val cabang = itemView.findViewById<TextView>(R.id.hppel)
        val jamoper = itemView.findViewById<TextView>(R.id.jamope)
        val hubungi = itemView.findViewById<Button>(R.id.hubungipel)
        val hapus = itemView.findViewById<Button>(R.id.lihatpel)
    }
}