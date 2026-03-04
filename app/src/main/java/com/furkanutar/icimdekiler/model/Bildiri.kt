package com.furkanutar.icimdekiler.model

data class Bildiri(
    val urunAdi: String = "",
    val aramaTerimi: String = "",
    val durum: String = "",
    val mesaj: String = "",
    val zaman: com.google.firebase.Timestamp? = null
)