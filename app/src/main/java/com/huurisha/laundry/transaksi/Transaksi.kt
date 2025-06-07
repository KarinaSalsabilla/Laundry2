package com.huurisha.laundry.transaksi

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.huurisha.laundry.R
import com.huurisha.laundry.adapter.TambahanTransaksiAdapter
import com.huurisha.laundry.konfirmasiData
import com.huurisha.laundry.modeldata.ModelTransaksiTambahan
import com.huurisha.laundry.pilihPelangganActivity
import com.huurisha.laundry.pilih_layanan
import com.huurisha.laundry.pilih_layanan_tambahan
import java.text.NumberFormat
import java.util.*

class Transaksi : AppCompatActivity() {
    private lateinit var tvPelangganNama : TextView
    private lateinit var tvPelangganNoHP : TextView
    private lateinit var tvLayananNama : TextView
    private lateinit var tvLayananHarga  : TextView
    private lateinit var rvLayananTambahan : RecyclerView
    private lateinit var btPilihPelanggan: Button
    private lateinit var btPilihLayanan: Button
    private lateinit var btTambahan: Button
    private lateinit var btProses: Button
    private val datalist  = mutableListOf<ModelTransaksiTambahan>()

    private val pilihPelanggan = 1
    private val pilihLayanan = 2
    private val pilihLayananTambahan = 3

    private var idPelanggan: String=""
    private var idCabang: String = ""
    private var namaPelanggan:String =""
    private var noHP:String=""
    private var idLayanan: String=""
    private var namaLayanan:String = ""
    private var hargaLayanan:String = ""
    private var idPegawai:String=""
    private lateinit var sharedPref: SharedPreferences
    private lateinit var tambahanAdapter: TambahanTransaksiAdapter

    // Fungsi untuk memformat harga ke format Indonesia
    private fun formatHarga(harga: Int): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        return numberFormat.format(harga)
    }

    // Fungsi untuk mengkonversi string harga ke integer
    private fun parseHarga(hargaString: String): Int {
        return try {
            hargaString.replace("Rp", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "")
                .trim()
                .toInt()
        } catch (e: Exception) {
            0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)
        sharedPref = getSharedPreferences("user_data", MODE_PRIVATE)
        idCabang = sharedPref.getString("idCabang",null).toString()
        idPegawai = sharedPref.getString("idPegawai",null).toString()
        init()

        // Restore data jika ada saved instance
        if (savedInstanceState != null) {
            restoreData(savedInstanceState)
        }

        FirebaseApp.initializeApp(this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = false
        rvLayananTambahan.layoutManager = layoutManager
        rvLayananTambahan.isNestedScrollingEnabled = false
        rvLayananTambahan.setHasFixedSize(false)

        tambahanAdapter = TambahanTransaksiAdapter(datalist)
        rvLayananTambahan.adapter = tambahanAdapter

        btPilihPelanggan.setOnClickListener{
            val intent = Intent(this, pilihPelangganActivity::class.java)
            startActivityForResult(intent, pilihPelanggan)
        }

        btPilihLayanan.setOnClickListener{
            val intent = Intent(this, pilih_layanan::class.java)
            startActivityForResult(intent, pilihLayanan)
        }

        btTambahan.setOnClickListener{
            if (idPelanggan.isEmpty() || idLayanan.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_customer_and_service), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, pilih_layanan_tambahan::class.java)
            startActivityForResult(intent, pilihLayananTambahan)
        }

        btProses.setOnClickListener {
            if (idPelanggan.isEmpty() || idLayanan.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_customer_and_service), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, konfirmasiData::class.java)

            // Data pelanggan dan layanan utama
            intent.putExtra("idPelanggan", idPelanggan)
            intent.putExtra("nama", namaPelanggan)
            intent.putExtra("telepon", noHP)
            intent.putExtra("idLayanan", idLayanan)
            intent.putExtra("namalayanan", namaLayanan)

            // Convert harga dari String ke Int untuk konsistensi
            val hargaInt = parseHarga(hargaLayanan)
            intent.putExtra("hargalayanan", hargaInt)

            // Format harga untuk ditampilkan
            intent.putExtra("hargalayanan_formatted", formatHarga(hargaInt))

            // Kirim data layanan tambahan sebagai ArrayList sederhana
            val tambahanNamaList = ArrayList<String>()
            val tambahanHargaList = ArrayList<Int>()
            val tambahanHargaFormattedList = ArrayList<String>()

            for (item in datalist) {
                tambahanNamaList.add(item.nama ?: "")
                val hargaTambahan = item.harga ?: 0
                tambahanHargaList.add(hargaTambahan)
                tambahanHargaFormattedList.add(formatHarga(hargaTambahan))
            }

            intent.putStringArrayListExtra("tambahan_nama", tambahanNamaList)
            intent.putIntegerArrayListExtra("tambahan_harga", tambahanHargaList)
            intent.putStringArrayListExtra("tambahan_harga_formatted", tambahanHargaFormattedList)

            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Simpan data saat rotasi layar
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Simpan data pelanggan
        outState.putString("idPelanggan", idPelanggan)
        outState.putString("namaPelanggan", namaPelanggan)
        outState.putString("noHP", noHP)

        // Simpan data layanan
        outState.putString("idLayanan", idLayanan)
        outState.putString("namaLayanan", namaLayanan)
        outState.putString("hargaLayanan", hargaLayanan)

        // Simpan data layanan tambahan
        val tambahanIds = ArrayList<String>()
        val tambahanNama = ArrayList<String>()
        val tambahanHarga = ArrayList<Int>()

        for (item in datalist) {
            tambahanIds.add(item.idLayananTambahan ?: "")
            tambahanNama.add(item.nama ?: "")
            tambahanHarga.add(item.harga ?: 0)
        }

        outState.putStringArrayList("tambahan_ids", tambahanIds)
        outState.putStringArrayList("tambahan_nama", tambahanNama)
        outState.putIntegerArrayList("tambahan_harga", tambahanHarga)
    }

    // Restore data setelah rotasi layar
    private fun restoreData(savedInstanceState: Bundle) {
        // Restore data pelanggan
        idPelanggan = savedInstanceState.getString("idPelanggan", "")
        namaPelanggan = savedInstanceState.getString("namaPelanggan", "")
        noHP = savedInstanceState.getString("noHP", "")

        // Restore data layanan
        idLayanan = savedInstanceState.getString("idLayanan", "")
        namaLayanan = savedInstanceState.getString("namaLayanan", "")
        hargaLayanan = savedInstanceState.getString("hargaLayanan", "")

        // Update UI dengan data yang di-restore
        if (namaPelanggan.isNotEmpty()) {
            tvPelangganNama.text = "${getString(R.string.customer_name)} : $namaPelanggan"
            tvPelangganNoHP.text = "${getString(R.string.phone_number)} : $noHP"
        }

        if (namaLayanan.isNotEmpty()) {
            val hargaInt = parseHarga(hargaLayanan)
            val hargaFormatted = formatHarga(hargaInt)
            tvLayananNama.text = "${getString(R.string.service_name)} : $namaLayanan"
            tvLayananHarga.text = "${getString(R.string.price)} : $hargaFormatted"
        }

        // Restore data layanan tambahan
        val tambahanIds = savedInstanceState.getStringArrayList("tambahan_ids")
        val tambahanNama = savedInstanceState.getStringArrayList("tambahan_nama")
        val tambahanHarga = savedInstanceState.getIntegerArrayList("tambahan_harga")

        if (tambahanIds != null && tambahanNama != null && tambahanHarga != null) {
            datalist.clear()
            for (i in tambahanIds.indices) {
                val modelTambahan = ModelTransaksiTambahan(
                    tambahanIds[i],
                    tambahanNama[i],
                    tambahanHarga[i]
                )
                datalist.add(modelTambahan)
            }
        }
    }

    fun init(){
        tvPelangganNama = findViewById(R.id.txt2)
        tvPelangganNoHP = findViewById(R.id.txt3)
        tvLayananNama = findViewById(R.id.txt4)
        tvLayananHarga = findViewById(R.id.txt5)
        rvLayananTambahan = findViewById(R.id.rvDatalayanantambahandepan)
        btPilihPelanggan = findViewById(R.id.btn1)
        btPilihLayanan = findViewById(R.id.btn2)
        btTambahan = findViewById(R.id.btnTambahan)
        btProses = findViewById(R.id.btnProses)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == pilihPelanggan){
            if(resultCode == RESULT_OK && data!= null) {
                idPelanggan = data.getStringExtra("idPelanggan").toString()
                val nama = data.getStringExtra("nama")
                val nomorHP = data.getStringExtra("noHP")
                tvPelangganNama.text = "${getString(R.string.customer_name)} : $nama"
                tvPelangganNoHP.text = "${getString(R.string.phone_number)} : $nomorHP"
                namaPelanggan = nama.toString()
                noHP = nomorHP.toString()
            }
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "${getString(R.string.cancel_select_customer)} $namaPelanggan", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if(requestCode == pilihLayanan){
            if(resultCode == RESULT_OK && data!= null) {
                idLayanan = data.getStringExtra("idLayanan").toString()
                val nama = data.getStringExtra("nama")
                val harga = data.getStringExtra("harga")

                // Format harga untuk tampilan
                val hargaInt = parseHarga(harga.toString())
                val hargaFormatted = formatHarga(hargaInt)

                tvLayananNama.text = "${getString(R.string.service_name)} : $nama"
                tvLayananHarga.text = "${getString(R.string.price)} : $hargaFormatted"
                namaLayanan = nama.toString()
                hargaLayanan = harga.toString()
            }
            if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "${getString(R.string.cancel_select_service)} $namaLayanan", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (requestCode == pilihLayananTambahan) {
            if (resultCode == RESULT_OK && data != null) {
                val idLayananTambahan = data.getStringExtra("idLayananTambahan").toString()
                val namaLayanan = data.getStringExtra("nama").toString()
                val hargaLayanan = data.getStringExtra("harga").toString()

                // Convert harga dari String ke Int
                val hargaInt = parseHarga(hargaLayanan)

                val modelTambahan = ModelTransaksiTambahan(idLayananTambahan, namaLayanan, hargaInt)
                datalist.add(modelTambahan)

                // Tampilkan Toast ketika item berhasil ditambahkan
                Toast.makeText(this, "$namaLayanan ${getString(R.string.successfully_added)}", Toast.LENGTH_SHORT).show()

                tambahanAdapter.notifyDataSetChanged()
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.cancel_add_additional_service), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
