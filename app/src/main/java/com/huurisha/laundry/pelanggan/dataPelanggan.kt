package com.huurisha.laundry.pelanggan

import android.content.Intent
import android.graphics.ColorSpace.Model
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.DataPelangganAdapter
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.pegawai.tambah_pegawai

class dataPelanggan : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pelanggan")
    lateinit var rvdataPelanggan: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah : FloatingActionButton
    lateinit var pelangganList: ArrayList<ModelPelanggan>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)
        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvdataPelanggan.layoutManager = layoutManager
        rvdataPelanggan.setHasFixedSize(true)
        pelangganList = arrayListOf<ModelPelanggan>()
        getData()
        fabDATA_PENGGUNA_Tambah.setOnClickListener {
            val intent = Intent(this, tambahPelanggan::class.java)
            intent.putExtra("judul", this.getString(R.string.tvpelanggan))
            intent.putExtra("idPelanggan","")
            intent.putExtra("namaPelanggan","")
            intent.putExtra("noHpPelanggan","")
            intent.putExtra("alamatPelanggan","")
            intent.putExtra("idCabang","")
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvdataPelanggan = findViewById(R.id.rvDataPelanggan)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_PELANGGAN_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idPelanggan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                if (snapshot.exists()){
                    pelangganList.clear()
                    for (dataSnapshot in snapshot.children){
                        val pegawai = dataSnapshot.getValue(ModelPelanggan::class.java)
                        pelangganList.add(pegawai!!)
                    }
                    val adapter = DataPelangganAdapter(pelangganList)
                    rvdataPelanggan.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@dataPelanggan,error.message,Toast.LENGTH_SHORT).show()
            }

        })

    }
}