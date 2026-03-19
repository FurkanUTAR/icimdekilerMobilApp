package com.furkanutar.icimdekiler.model

data class Bildiri(
    val urunAdi: String = "",
    val barkodNo: String = "",
    val aramaTerimi: String = "",
    val durum: String = "",
    val mesaj: String = "",
    val tarih: com.google.firebase.Timestamp? = null
)