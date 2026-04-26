package com.furkanutar.icimdekiler.model

class Urunler(
    var barkodNo: String?,
    var urunAdi: String?,
    var icindekiler: String?,
    var gorselUrl: String?,
    var documentId: String?,
    var kalori: Int = 0,
    var protein: Float = 0f,
    var karbonhidrat: Float = 0f,
    var yag: Float = 0f
)