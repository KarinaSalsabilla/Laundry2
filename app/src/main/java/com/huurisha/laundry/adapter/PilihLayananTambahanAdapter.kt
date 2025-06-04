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
import com.huurisha.laundry.modeldata.ModelLayanan
import com.huurisha.laundry.modeldata.ModelTambahan
import java.text.NumberFormat
import java.util.Locale

class PilihLayananTambahanAdapter  (private val ListTambahan: ArrayList<ModelTambahan>) : RecyclerView.Adapter<PilihLayananTambahanAdapter .Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_layanantambahan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: PilihLayananTambahanAdapter.Viewholder, position: Int) {
        val nomor = position + 1
        val item = ListTambahan[position]
        holder.tvid.text = "[$nomor]"
        holder.tvNama.text = item.namabarang
        val hargaDouble = item.harga?.toDoubleOrNull() ?: 0.0
        holder.harga.text = "Harga= ${formatRupiah(hargaDouble)}"

        holder.cvCard.setOnClickListener {
            val intent = Intent()
            intent.putExtra("idtambahan", item.idcabang)
            intent.putExtra("nama", item.namabarang)
            intent.putExtra("harga", item.harga)
            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }
    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    override fun getItemCount(): Int {
        return ListTambahan.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardpilihlayanantambahan)
        val tvid = itemView.findViewById<TextView>(R.id.idpilihlayanantambahan)
        val tvNama = itemView.findViewById<TextView>(R.id.barang)
        val harga = itemView.findViewById<TextView>(R.id.hargalayanantambahan)
    }
}