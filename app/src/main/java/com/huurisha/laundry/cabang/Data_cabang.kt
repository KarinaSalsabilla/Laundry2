package com.huurisha.laundry.cabang

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
import com.huurisha.laundry.adapter.DataCabangAdapter
import com.huurisha.laundry.adapter.DataLayananAdapter
import com.huurisha.laundry.layanan.tambahLayanan
import com.huurisha.laundry.modeldata.ModelCabang
import com.huurisha.laundry.modeldata.ModelLayanan

class Data_cabang : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("cabang")
    lateinit var rvdatacabang: RecyclerView
    lateinit var fabDATA_PENGGUNA_Tambah : FloatingActionButton
    lateinit var cabangList: ArrayList<ModelCabang>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)

        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvdatacabang.layoutManager = layoutManager
        rvdatacabang.setHasFixedSize(true)
        cabangList = arrayListOf<ModelCabang>()
        getData()
        tekan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun init(){
        rvdatacabang = findViewById(R.id.rvDataCabang)
        fabDATA_PENGGUNA_Tambah = findViewById(R.id.fabDATA_cabang_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idCabang").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot){
                if (snapshot.exists()){
                    cabangList.clear()
                    for (dataSnapshot in snapshot.children){
                        val pegawai = dataSnapshot.getValue(ModelCabang::class.java)
                        cabangList.add(pegawai!!)
                    }
                    val adapter = DataCabangAdapter(cabangList)
                    rvdatacabang.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Data_cabang,error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }
    fun tekan(){
        fabDATA_PENGGUNA_Tambah.setOnClickListener{
            val intent = Intent(this, Tambah_cabang::class.java)
            startActivity(intent)
        }
    }
}