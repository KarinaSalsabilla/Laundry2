package com.huurisha.laundry.modeldata

data class ModelInvoice(
    var idInvoice: String? = null,
    var idTransaksi: String? = null,
    var namaPelanggan: String? = null,
    var noHpPelanggan: String? = null,
    var namaPegawai: String? = null,
    var namaLayanan: String? = null,
    var hargaLayanan: Int? = null,
    var layananTambahan: ArrayList<ModelTransaksiTambahan>? = null,
    var totalTambahan: Int? = null,
    var totalBayar: Int? = null,
    var tanggalWaktu: String? = null,
    var timestamp: Long? = null
)