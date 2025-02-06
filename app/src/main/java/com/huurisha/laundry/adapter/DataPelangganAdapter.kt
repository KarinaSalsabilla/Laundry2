package com.huurisha.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPelanggan



class DataPelangganAdapter (private val ListPelanggan: ArrayList<ModelPelanggan>) : RecyclerView.Adapter<DataPelangganAdapter.Viewholder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapelanggan, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListPelanggan[position]
        holder.tvid.text = item.idPelanggan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = item.alamatPelanggan
        holder.tvNoHP.text = item.noHpPelanggan
        holder.cabangpelang.text = item.idCabang
        holder.cvCard.setOnClickListener{

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

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
        val btHubungi = itemView.findViewById<Button>(R.id.hubungipel)
        val btLihat = itemView.findViewById<Button>(R.id.lihatpel)




    }
}