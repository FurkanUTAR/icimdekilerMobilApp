package com.furkanutar.icimdekiler.model

import com.google.gson.annotations.SerializedName

data class OFFYanit(
    @SerializedName("status") val durum: Int, // 1 ise ürün bulundu, 0 ise bulunamadı
    @SerializedName("product") val urun: OFFUrun?
)

data class OFFUrun(
    @SerializedName("code") val barkodNo: String?,
    @SerializedName("product_name") val urunAdi: String?,
    @SerializedName("ingredients_text_tr") val icindekilerTr: String?, // Önce buraya bakacağız
    @SerializedName("ingredients_text") val icindekilerGenel: String?, // Burası yedek plan
    @SerializedName("image_url") val gorselUrl: String?
)