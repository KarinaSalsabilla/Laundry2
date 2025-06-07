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
import com.huurisha.laundry.adapter.DataLayananAdapter.Viewholder
import com.huurisha.laundry.modeldata.ModelLayanan
import java.text.NumberFormat
import java.util.Locale

class PilihLayananAdapter (private val ListLayanan: ArrayList<ModelLayanan>) : RecyclerView.Adapter<PilihLayananAdapter .Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pilih_layanan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: PilihLayananAdapter.Viewholder, position: Int) {
        val nomor = position + 1
        val item = ListLayanan[position]
        holder.tvid.text = "[$nomor]"
        holder.tvNama.text = item.namaLayanan
        val hargaDouble = item.hargaLayanan?.toDoubleOrNull() ?: 0.0
        holder.harga.text = "${appContext.getString(R.string.harga)} =  ${formatRupiah(hargaDouble)}"

        holder.cvCard.setOnClickListener {
            val intent = Intent()
            intent.putExtra("idLayanan", item.idLayanan)
            intent.putExtra("nama", item.namaLayanan)
            intent.putExtra("harga", item.hargaLayanan)
            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }
    private fun formatRupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    override fun getItemCount(): Int {
        return ListLayanan.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardpilihlayanan)
        val tvid = itemView.findViewById<TextView>(R.id.idpilihlayanan)
        val tvNama = itemView.findViewById<TextView>(R.id.namapilihlayanan)
        val harga = itemView.findViewById<TextView>(R.id.harga)
    }
}