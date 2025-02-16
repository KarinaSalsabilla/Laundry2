package com.huurisha.laundry.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataPelangganAdapter.Viewholder
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelPelanggan

class DataPegawaiAdapter (private val ListPegawai: ArrayList<ModelPegawai>) : RecyclerView.Adapter<DataPegawaiAdapter.Viewholder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapegawai, parent, false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListPegawai[position]
        holder.tvid.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = item.alamatPegawai
        holder.tvNoHP.text = item.noHpPegawai
        holder.terdaftar.text = item.terdaftar
        holder.cabangpelang.text = item.idCabangPegawai
        holder.cvCard.setOnClickListener{

        }
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }

    }
    override fun getItemCount(): Int {
        return ListPegawai.size

    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.carddatapegawai)
        val tvid = itemView.findViewById<TextView>(R.id.idpegawai)
        val tvNama = itemView.findViewById<TextView>(R.id.namapega)
        val tvAlamat = itemView.findViewById<TextView>(R.id.alamatpegawai)
        val tvNoHP = itemView.findViewById<TextView>(R.id.hppegawai)
        val terdaftar = itemView.findViewById<TextView>(R.id.daftar)
        val cabangpelang = itemView.findViewById<TextView>(R.id.cabangpegawai)
        val btHubungi = itemView.findViewById<Button>(R.id.hubungi)
        val btLihat = itemView.findViewById<Button>(R.id.lihat)




    }
}
