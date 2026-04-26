package com.furkanutar.icimdekiler.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// ─── Token ────────────────────────────────────────────────────────────────────
data class FsTokenYanit(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in")   val expiresIn: Int
)

// ─── Arama (food.search) ───────────────────────────────────────────────────────
data class FsSearchYanit(
    @SerializedName("foods") val foods: FsFoods?
)

data class FsFoods(
    // Tek sonuçta object, birden fazlasında array gelir
    @SerializedName("food") val food: JsonElement?
)

data class FsFoodItem(
    @SerializedName("food_id")   val foodId: String?,
    @SerializedName("food_name") val foodName: String?,
    @SerializedName("brand_name") val brandName: String?
)

// ─── Ürün Detay (food.get) ────────────────────────────────────────────────────
data class FsFoodYanit(
    @SerializedName("food") val food: FsFood?
)

data class FsFood(
    @SerializedName("food_id")   val foodId: String?,
    @SerializedName("food_name") val foodName: String?,
    @SerializedName("servings")  val servings: FsServings?
)

data class FsServings(
    // Tek porsiyon object, birden fazlasında array gelir
    @SerializedName("serving") val serving: JsonElement?
)

data class FsServing(
    @SerializedName("calories")          val calories: String?,
    @SerializedName("protein")           val protein: String?,
    @SerializedName("carbohydrate")      val carbohydrate: String?,
    @SerializedName("fat")               val fat: String?,
    @SerializedName("fiber")             val fiber: String?,
    @SerializedName("sugar")             val sugar: String?,
    @SerializedName("sodium")            val sodium: String?,
    @SerializedName("is_default")        val isDefault: String?,
    @SerializedName("serving_description") val servingDescription: String?,
    @SerializedName("metric_serving_amount") val metricServingAmount: String?,
    @SerializedName("metric_serving_unit") val metricServingUnit: String?
)
