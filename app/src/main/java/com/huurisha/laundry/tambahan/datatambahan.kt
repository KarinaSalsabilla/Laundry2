package com.huurisha.laundry.tambahan

import android.content.Intent
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
import com.huurisha.laundry.adapter.DataTambahanAdapter
import com.huurisha.laundry.modeldata.ModelPelanggan
import com.huurisha.laundry.modeldata.ModelTambahan
import com.huurisha.laundry.pelanggan.tambahPelanggan

class datatambahan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("tambahan")
    lateinit var rvdataTambahan: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah : FloatingActionButton
    lateinit var tambahanList: ArrayList<ModelTambahan>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_tambahan)
        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvdataTambahan.layoutManager = layoutManager
        rvdataTambahan.setHasFixedSize(true)
        tambahanList = arrayListOf<ModelTambahan>()
        getData()
        tekan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvdataTambahan = findViewById(R.id.rvDataTambahan)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idTambahan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                if (snapshot.exists()){
                    tambahanList.clear()
                    for (dataSnapshot in snapshot.children){
                        val pegawai = dataSnapshot.getValue(ModelTambahan::class.java)
                        tambahanList.add(pegawai!!)
                    }
                    val adapter = DataTambahanAdapter(tambahanList)
                    rvdataTambahan.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@datatambahan,error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }
    fun tekan(){
        fabDATA_PENGGUNA_Tambah.setOnClickListener{
            val intent = Intent(this, Tambahan::class.java)
            startActivity(intent)
        }
    }
}