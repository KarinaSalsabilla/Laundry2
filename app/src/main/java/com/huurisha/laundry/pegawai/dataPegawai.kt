package com.huurisha.laundry.pegawai

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
import com.huurisha.laundry.adapter.DataPegawaiAdapter
import com.huurisha.laundry.adapter.DataPelangganAdapter
import com.huurisha.laundry.modeldata.ModelPegawai
import com.huurisha.laundry.modeldata.ModelPelanggan

class dataPegawai : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("pegawai")
    lateinit var rvdataPegawai: RecyclerView
    lateinit var pegawaiList: ArrayList<ModelPegawai>
    lateinit var tambahPega : FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)
        init()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        rvdataPegawai.layoutManager = layoutManager
        rvdataPegawai.setHasFixedSize(true)
        pegawaiList = arrayListOf<ModelPegawai>()
        getData()
        tekan()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun init(){
        rvdataPegawai = findViewById(R.id.rvDataPegawai)
        tambahPega = findViewById(R.id.fabDATA_PEGAWAI_Tambah)
    }

    fun getData(){
        val query = myRef.orderByChild("idPegawai").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    pegawaiList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                        pegawaiList.add(pegawai!!)
                    }
                    val adapter = DataPegawaiAdapter(pegawaiList)
                    rvdataPegawai.adapter = adapter
                    adapter.notifyDataSetChanged()
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@dataPegawai,error.message, Toast.LENGTH_SHORT).show()
            }

        })

    }

            fun tekan() {
                tambahPega.setOnClickListener {
                    val intent = Intent(this, tambah_pegawai::class.java)
                    startActivity(intent)
                }
            }
        }
