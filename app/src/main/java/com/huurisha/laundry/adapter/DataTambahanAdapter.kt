package com.huurisha.laundry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelTambahan
import java.text.NumberFormat
import java.util.Locale

class DataTambahanAdapter(
    private val context: Context, // Tambah ini
    private val ListTambahan: ArrayList<ModelTambahan>
) : RecyclerView.Adapter<DataTambahanAdapter.Viewholder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatatambahan, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListTambahan[position]
        holder.tvid.text = item.idcabang
        holder.tvNama.text = "${context.getString(R.string.namabarang)} = ${item.namabarang}"
        val hargaDouble = item.harga?.toDoubleOrNull() ?: 0.0
        holder.harga.text = "${context.getString(R.string.hargabarang)} = ${formatRupiah(hargaDouble)}"
        holder.cabang.text = "${context.getString(R.string.cabang)} = ${item.namacabang}"
        holder.cvCard.setOnClickListener{

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
        val cvCard = itemView.findViewById<View>(R.id.cardataTambahan)
        val tvid = itemView.findViewById<TextView>(R.id.idtambahan)
        val tvNama = itemView.findViewById<TextView>(R.id.namabarang)
        val harga= itemView.findViewById<TextView>(R.id.harga)
        val cabang = itemView.findViewById<TextView>(R.id.cabangtambahan)




    }
}
