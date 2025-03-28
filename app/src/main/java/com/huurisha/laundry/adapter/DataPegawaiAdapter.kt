package com.huurisha.laundry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataPelangganAdapter.Viewholder
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.pegawai.tambah_pegawai

class DataPegawaiAdapter (private val ListPegawai: ArrayList<ModelPegawai>) : RecyclerView.Adapter<DataPegawaiAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapegawai, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListPegawai[position]
        
        holder.tvid.text = item.idPegawai
        holder.tvNama.text = item.namaPegawai
        holder.tvAlamat.text = "Alamat= ${item.alamatPegawai}"
        holder.tvNoHP.text = "No Hp= ${item.noHpPegawai}"
        holder.cabangpelang.text = "Cabang= ${item.idCabangPegawai}"
        holder.terdaftar.text = "Terdaftar= ${item.terdaftar}"
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }

        holder.cvCard.setOnClickListener {
            val intent = Intent(appContext, tambah_pegawai::class.java)
            intent.putExtra("judul","Edit Pegawai")
            intent.putExtra("idPegawai",item.idPegawai)
            intent.putExtra("namaPegawai",item.namaPegawai)
            intent.putExtra("noHpPegawai",item.noHpPegawai)
            intent.putExtra("alamatPegawai",item.alamatPegawai)
            intent.putExtra("idCabangPegawai",item.idCabangPegawai)
            appContext.startActivity(intent)


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
        val cabangpelang = itemView.findViewById<TextView>(R.id.cabangpegawai)
        val terdaftar = itemView.findViewById<TextView>(R.id.daftar)
        val btHubungi = itemView.findViewById<Button>(R.id.hubungi)
        val btLihat = itemView.findViewById<Button>(R.id.lihat)




    }
}
