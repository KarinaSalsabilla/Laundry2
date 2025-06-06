package com.huurisha.laundry.layanan

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
import com.huurisha.laundry.adapter.DataLayananAdapter
import com.huurisha.laundry.modeldata.ModelLayanan

class dataLayanan : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("layanan")
    lateinit var rvdataLayanan: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah : FloatingActionButton
    lateinit var layananList: ArrayList<ModelLayanan>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_layanan)

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvdataLayanan.layoutManager = layoutManager
        rvdataLayanan.setHasFixedSize(true)
        layananList = arrayListOf<ModelLayanan>()
        getData()
        tekan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvdataLayanan = findViewById(R.id.rvDataLayanan)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_layanan_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idLayanan").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                if (snapshot.exists()){
                    layananList.clear()
                    for (dataSnapshot in snapshot.children){
                        val pegawai = dataSnapshot.getValue(ModelLayanan::class.java)
                        layananList.add(pegawai!!)
                    }
                    val adapter = DataLayananAdapter(this@dataLayanan,layananList)
                    rvdataLayanan.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@dataLayanan,error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }
    fun tekan(){
        fabDATA_PENGGUNA_Tambah.setOnClickListener{
            val intent = Intent(this,tambahLayanan::class.java)
            startActivity(intent)
        }
    }
}