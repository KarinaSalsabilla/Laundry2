package com.huurisha.laundry.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.huurisha.laundry.R
import com.huurisha.laundry.modeldata.ModelTambahan
import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
import java.text.NumberFormat
import java.util.Locale

class TambahanTransaksiAdapter(
    private val ListTambahan: MutableList<ModelTransaksiTambahan>,
    private val onItemRemoved: (() -> Unit)? = null // Callback untuk notify parent
) : RecyclerView.Adapter<TambahanTransaksiAdapter.Viewholder>() {

    lateinit var appContext: Context
    lateinit var databaseReference: DatabaseReference

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Viewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pilihlayanantambahandepan, parent, false)
        appContext = parent.context
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: TambahanTransaksiAdapter.Viewholder, position: Int) {
        val nomor = position + 1
        val item = ListTambahan[position]
        holder.tvid.text = "[$nomor]"
        holder.tvNama.text = item.nama
        val hargaDouble = item.harga ?: 0
        holder.harga.text = "${appContext.getString(R.string.harga)} =  ${formatRupiah(hargaDouble)}"

        holder.cvCard.setOnClickListener {
            val intent = Intent()
            intent.putExtra("idLayananTambahan", item.idLayananTambahan)
            intent.putExtra("nama", item.nama)
            intent.putExtra("harga", item.harga)

            holder.cvCard.isClickable = false
            holder.cvCard.isFocusable = false
        }

        holder.btnHapus.setOnClickListener {
            // Simpan nama item sebelum dihapus untuk Toast
            val namaItem = item.nama ?: "Item"

            // Hapus item dari list
            ListTambahan.removeAt(position)

            // Notify adapter tentang perubahan
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, ListTambahan.size)

            // Tampilkan Toast ketika item dihapus
            val message = appContext.getString(R.string.item_deleted, namaItem)
            Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()


            // Panggil callback untuk memberitahu parent activity/fragment
            onItemRemoved?.invoke()
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        return format.format(amount)
    }

    override fun getItemCount(): Int {
        return ListTambahan.size
    }

    class Viewholder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val cvCard = itemView.findViewById<View>(R.id.cardpilihlayanantambahandepan)
        val tvid = itemView.findViewById<TextView>(R.id.idpilihlayanantambahan)
        val tvNama = itemView.findViewById<TextView>(R.id.barang)
        val harga = itemView.findViewById<TextView>(R.id.hargalayanantambahan)
        val btnHapus = itemView.findViewById<ImageView>(R.id.btnHapus)
    }
}