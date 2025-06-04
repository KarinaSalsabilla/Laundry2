package com.huurisha.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelLayanan
import com.huurisha.laundry.modeldata.ModelPegawai
import java.text.NumberFormat
import java.util.Locale

class DataLayananAdapter (private val ListLayanan: ArrayList<ModelLayanan>) : RecyclerView.Adapter<DataLayananAdapter.Viewholder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatalayanan, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListLayanan[position]
        holder.tvid.text = item.idLayanan
        holder.tvNama.text = item.namaLayanan
        val hargaDouble = item.hargaLayanan?.toDoubleOrNull() ?: 0.0
        holder.harga.text = "Harga: ${formatRupiah(hargaDouble)}"
        holder.cabang.text = "Cabang= ${item.cabangLayanan}"

        holder.cvCard.setOnClickListener {

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
            val cvCard = itemView.findViewById<View>(R.id.carddatalayanan)
            val tvid = itemView.findViewById<TextView>(R.id.idlayanan)
            val tvNama = itemView.findViewById<TextView>(R.id.nama)
            val harga = itemView.findViewById<TextView>(R.id.harga)
            val cabang = itemView.findViewById<TextView>(R.id.cabanglayanan)


        }
    }

