package com.huurisha.laundry.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelCabang
import com.huurisha.laundry.modeldata.ModelLayanan

class DataCabangAdapter (private val ListCabang: ArrayList<ModelCabang>) : RecyclerView.Adapter<DataCabangAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.carddatacabang, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = ListCabang[position]
        holder.tvid.text = item.idcabang
        holder.tvNama.text = item.namacabang
        holder.harga.text = "Alamat= ${item.alamatcabang}"
        holder.cabang.text = "NoHp= ${item.nohp}"
        holder.jamoper.text = "Jam Operasional= ${item.jamopera}"

        holder.cvCard.setOnClickListener {

        }

        holder.hubungi.setOnClickListener {

        }

        holder.lihat.setOnClickListener {

        }
    }
    override fun getItemCount(): Int {
        return ListCabang.size

    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardcabang)
        val tvid = itemView.findViewById<TextView>(R.id.idcabang)
        val tvNama = itemView.findViewById<TextView>(R.id.namapel)
        val harga = itemView.findViewById<TextView>(R.id.alamatpel)
        val cabang = itemView.findViewById<TextView>(R.id.hppel)
        val jamoper = itemView.findViewById<TextView>(R.id.jamope)
        val hubungi = itemView.findViewById<Button>(R.id.hubungipel)
        val lihat = itemView.findViewById<Button>(R.id.lihatpel)


    }
}
