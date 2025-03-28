package com.huurisha.laundry.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.pegawai.tambah_pegawai
import com.huurisha.laundry.pelanggan.tambahPelanggan


class DataPelangganAdapter (private val ListPelanggan: ArrayList<ModelPelanggan>) : RecyclerView.Adapter<DataPelangganAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatapelanggan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListPelanggan[position]
        holder.tvid.text = item.idPelanggan
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = "Alamat= ${item.alamatPelanggan}"
        holder.tvNoHP.text = "No Hp= ${item.noHpPelanggan}"
        holder.cabangpelang.text = "Cabang= ${item.idCabang}"
        holder.btHubungi.setOnClickListener{

        }
        holder.btLihat.setOnClickListener{

        }


        holder.cvCard.setOnClickListener {
            val intent = Intent(appContext, tambahPelanggan::class.java)
            intent.putExtra("judul","Edit Pelanggan")
            intent.putExtra("idPelanggan",item.idPelanggan)
            intent.putExtra("namaPelanggan",item.namaPelanggan)
            intent.putExtra("noHpPelanggan",item.noHpPelanggan)
            intent.putExtra("alamatPelanggan",item.alamatPelanggan)
            intent.putExtra("idCabang",item.idCabang)
            appContext.startActivity(intent)


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