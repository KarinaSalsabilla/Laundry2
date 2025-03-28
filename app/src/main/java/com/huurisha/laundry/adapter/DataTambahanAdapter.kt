package com.huurisha.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelTambahan

class DataTambahanAdapter (private val ListTambahan: ArrayList<ModelTambahan>) : RecyclerView.Adapter<DataTambahanAdapter.Viewholder>() {
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
        holder.tvNama.text = "Nama Barang= ${item.namabarang}"
        holder.harga.text = "Harga = ${item.harga}"
        holder.cabang.text = "Cabang= ${item.namacabang}"
        holder.cvCard.setOnClickListener{

        }

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
