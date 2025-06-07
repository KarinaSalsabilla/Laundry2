package com.huurisha.laundry.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelTambahan
import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
import java.text.NumberFormat
import java.util.Locale

class KonfirmasiDataAdapter (private val ListTambahan: ArrayList<ModelTransaksiTambahan>) : RecyclerView.Adapter<KonfirmasiDataAdapter .Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.konfirmasi_layanan_tambahan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: KonfirmasiDataAdapter.Viewholder, position: Int) {
        val nomor = position + 1
        val item = ListTambahan[position]
        holder.tvid.text = "[$nomor]"
        holder.tvNama.text = item.nama
        val hargaDouble = item.harga ?: 0
        holder.harga.text = "${appContext.getString(R.string.harga)} = ${formatRupiah(hargaDouble)}"

        holder.cvCard.setOnClickListener {
            val intent = Intent()
            intent.putExtra("idLayananTambahan", item.idLayananTambahan)
            intent.putExtra("nama", item.nama)
            intent.putExtra("harga", item.harga)
//            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
//            (appContext as Activity).finish()
        }
    }
    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    override fun getItemCount(): Int {
        return ListTambahan.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.konfirmasidatalayanan)
        val tvid = itemView.findViewById<TextView>(R.id.idpilihlayanan)
        val tvNama = itemView.findViewById<TextView>(R.id.namapilihtambahan)
        val harga = itemView.findViewById<TextView>(R.id.hargalayanantambahan)
    }
}