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
import com.huurisha.laundry.modeldata.ModelPelanggan

class PilihPelangganAdapter(private val pelangganList: ArrayList<ModelPelanggan>) : RecyclerView.Adapter<PilihPelangganAdapter.Viewholder>() {
    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardpilihpelanggan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val nomor = position + 1
        val item = pelangganList[position]
        holder.tvid.text = "[$nomor]"
        holder.tvNama.text = item.namaPelanggan
        holder.tvAlamat.text = "Alamat= ${item.alamatPelanggan}"
        holder.tvNoHP.text = "No Hp= ${item.noHpPelanggan}"
        holder.cvCard.setOnClickListener {
            val intent = Intent()
            intent.putExtra("idPelanggan", item.idPelanggan)
            intent.putExtra("nama", item.namaPelanggan)
            intent.putExtra("noHP", item.noHpPelanggan)
            (appContext as Activity).setResult(Activity.RESULT_OK, intent)
            (appContext as Activity).finish()
        }
    }

    override fun getItemCount(): Int {
        return pelangganList.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardpilihpelanggan)
        val tvid = itemView.findViewById<TextView>(R.id.idpilihpelanggan)
        val tvNama = itemView.findViewById<TextView>(R.id.namapilihpelanggan)
        val tvAlamat = itemView.findViewById<TextView>(R.id.alamatpilihpelanggan)
        val tvNoHP = itemView.findViewById<TextView>(R.id.nohp)
    }
}