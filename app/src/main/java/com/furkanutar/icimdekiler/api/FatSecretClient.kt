package com.furkanutar.icimdekiler.api

import android.util.Base64
import android.util.Log
import com.furkanutar.icimdekiler.BuildConfig
import com.furkanutar.icimdekiler.model.FsFoodItem
import com.furkanutar.icimdekiler.model.FsFoodYanit
import com.furkanutar.icimdekiler.model.FsSearchYanit
import com.furkanutar.icimdekiler.model.FsServing
import com.furkanutar.icimdekiler.model.FsTokenYanit
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// ─── Retrofit Interface'leri ──────────────────────────────────────────────────

interface FatSecretTokenService {
    @POST("connect/token")
    @FormUrlEncoded
    suspend fun tokenAl(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String
    ): FsTokenYanit
}

interface FatSecretFoodService {
    @GET("rest/server.api")
    suspend fun ara(
        @Header("Authorization") authorization: String,
        @Query("method") method: String,
        @Query("search_expression") arama: String,
        @Query("format") format: String,
        @Query("max_results") maxSonuc: Int,
        @Query("region") region: String = "TR",
        @Query("language") language: String = "tr"
    ): FsSearchYanit

    @GET("rest/server.api")
    suspend fun besinAl(
        @Header("Authorization") authorization: String,
        @Query("method") method: String,
        @Query("food_id") foodId: String,
        @Query("format") format: String
    ): FsFoodYanit

    @GET("rest/server.api")
    suspend fun barkodAra(
        @Header("Authorization") authorization: String,
        @Query("method") method: String,
        @Query("barcode") barcode: String,
        @Query("format") format: String
    ): JsonObject
}

// ─── FatSecret Client ─────────────────────────────────────────────────────────

object FatSecretClient {

    private const val TAG = "FatSecretClient"

    private val loggingInterceptor = HttpLoggingInterceptor { msg ->
        Log.d(TAG, msg)
    }.apply { level = HttpLoggingInterceptor.Level.BODY }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val tokenService: FatSecretTokenService by lazy {
        Retrofit.Builder()
            .baseUrl("https://oauth.fatsecret.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretTokenService::class.java)
    }

    private val foodService: FatSecretFoodService by lazy {
        Retrofit.Builder()
            .baseUrl("https://platform.fatsecret.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FatSecretFoodService::class.java)
    }

    private val gson = Gson()

    private var cachedToken: String? = null
    private var tokenSonGecerlilik: Long = 0L

    private suspend fun accessTokenAl(): String {
        val simdi = System.currentTimeMillis()
        if (cachedToken != null && simdi < tokenSonGecerlilik) {
            return cachedToken!!
        }

        val credentials = "${BuildConfig.FATSECRET_CLIENT_ID}:${BuildConfig.FATSECRET_CLIENT_SECRET}"
        val kimlik = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        return try {
            val yanit = tokenService.tokenAl(
                authorization = "Basic $kimlik",
                grantType = "client_credentials",
                scope = "basic barcode"
            )
            cachedToken = yanit.accessToken
            tokenSonGecerlilik = simdi + (yanit.expiresIn - 60) * 1000L
            yanit.accessToken
        } catch (e: HttpException) {
            val body = e.response()?.errorBody()?.string() ?: "boş"
            Log.e(TAG, "Token HTTP ${e.code()}: $body")
            throw e
        }
    }

    suspend fun besinDegerleriniAl(urunAdi: String, barkodNo: String? = null): FsServing? {
        return try {
            val token  = accessTokenAl()
            val bearer = "Bearer $token"
            var foodId: String? = null

            // 1. Önce Barkod ile aramayı dene
            if (!barkodNo.isNullOrEmpty()) {
                Log.d(TAG, "Barkod ile aranıyor: $barkodNo")
                try {
                    val barcodeYanit = foodService.barkodAra(
                        authorization = bearer,
                        method        = "food.find_id_for_barcode",
                        barcode       = barkodNo,
                        format        = "json"
                    )
                    
                    if (barcodeYanit.has("food_id") && barcodeYanit.getAsJsonObject("food_id").has("value")) {
                        val id = barcodeYanit.getAsJsonObject("food_id").get("value").asString
                        if (id != "0") {
                            foodId = id
                            Log.d(TAG, "Barkod ile food_id bulundu: $foodId")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Barkod arama hatası: ${e.message}")
                }
            }

            // 2. Eğer barkod bulunamadıysa isim ile ara
            if (foodId == null) {
                val temizAramaIsmi = temizle(urunAdi)
                Log.d(TAG, "İsim ile aranıyor: \"$temizAramaIsmi\"")
                try {
                    val aramaYanit = foodService.ara(
                        authorization = bearer,
                        method        = "foods.search",
                        arama         = temizAramaIsmi,
                        format        = "json",
                        maxSonuc      = 10
                    )
                    
                    foodId = aramaYanitindanIdAl(aramaYanit, temizAramaIsmi)
                } catch (e: Exception) {
                    Log.e(TAG, "İsimle arama hatası: ${e.message}")
                }
            }

            if (foodId == null) {
                Log.w(TAG, "\"$urunAdi\" için sonuç bulunamadı.")
                return null
            }

            // 3. Besin değerlerini al
            val besinYanit = foodService.besinAl(
                authorization = bearer,
                method        = "food.get.v4",
                foodId        = foodId,
                format        = "json"
            )

            val serving = besinYanitindanYuzGramPorsiyonAl(besinYanit)
            Log.d(TAG, "Besin ✅  Kal=${serving?.calories} P=${serving?.protein} K=${serving?.carbohydrate} Y=${serving?.fat}")
            serving
        } catch (e: Exception) {
            Log.e(TAG, "Genel hata: ${e.message}")
            null
        }
    }

    private fun aramaYanitindanIdAl(yanit: FsSearchYanit, arananUrunAdi: String): String? {
        val element = yanit.foods?.food ?: return null
        return try {
            val liste = if (element.isJsonArray) {
                gson.fromJson(element, Array<FsFoodItem>::class.java).toList()
            } else if (element.isJsonObject) {
                listOf(gson.fromJson(element, FsFoodItem::class.java))
            } else emptyList()

            val arananKelimeler = temizle(arananUrunAdi).split(Regex("\\s+")).filter { it.length > 2 }

            // Daha sıkı eşleştirme: Aranan kelimelerin en az %70'i sonuç isminde geçmeli
            val uygunUrun = liste.firstOrNull { item ->
                val sonucIsmi = temizle("${item.brandName.orEmpty()} ${item.foodName.orEmpty()}")
                if (arananKelimeler.isEmpty()) true 
                else {
                    val eslesenKelimeSayisi = arananKelimeler.count { sonucIsmi.contains(it) }
                    val oran = eslesenKelimeSayisi.toFloat() / arananKelimeler.size
                    oran >= 0.7f // %70 eşleşme barajı
                }
            }

            uygunUrun?.foodId
        } catch (e: Exception) {
            Log.e(TAG, "food_id parse hatası: ${e.message}")
            null
        }
    }

    private fun temizle(metin: String): String {
        return metin.lowercase()
            .replace("ç", "c").replace("ğ", "g").replace("ı", "i")
            .replace("ö", "o").replace("ş", "s").replace("ü", "u")
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()
    }

    private fun besinYanitindanYuzGramPorsiyonAl(yanit: FsFoodYanit): FsServing? {
        val element = yanit.food?.servings?.serving ?: return null
        return try {
            val liste: List<FsServing> = if (element.isJsonArray) {
                gson.fromJson(element, Array<FsServing>::class.java).toList()
            } else if (element.isJsonObject) {
                listOf(gson.fromJson(element, FsServing::class.java))
            } else emptyList()

            // 1. Önce tam olarak 100g olanı ara
            val yuzGram = liste.firstOrNull { 
                (it.metricServingAmount == "100.000" && it.metricServingUnit == "g") || 
                it.servingDescription?.contains("100 g", ignoreCase = true) == true ||
                it.servingDescription?.contains("100g", ignoreCase = true) == true
            }
            if (yuzGram != null) {
                Log.d(TAG, "100g porsiyon direkt bulundu.")
                return yuzGram
            }
            
            // 2. Yoksa is_default=1 olanı al
            val varsayilan = liste.firstOrNull { it.isDefault == "1" } ?: liste.firstOrNull() ?: return null
            
            // 3. Varsayılanı 100g'a oranla (Eğer gram veya ml cinsindeyse)
            val miktar = varsayilan.metricServingAmount?.toFloatOrNull()
            val birim = varsayilan.metricServingUnit?.lowercase()
            
            if (miktar != null && miktar > 0 && (birim == "g" || birim == "ml" || birim == "oz")) {
                var gercekMiktar = miktar
                if (birim == "oz") gercekMiktar *= 28.3495f // oz to gram
                
                val oran = 100f / gercekMiktar
                Log.d(TAG, "Varsayılan porsiyon ($gercekMiktar g) 100g'a oranlanıyor (çarpan: $oran).")
                
                return varsayilan.copy(
                    calories = ((varsayilan.calories?.toFloatOrNull() ?: 0f) * oran).toInt().toString(),
                    protein = "%.1f".format((varsayilan.protein?.toFloatOrNull() ?: 0f) * oran).replace(",", "."),
                    carbohydrate = "%.1f".format((varsayilan.carbohydrate?.toFloatOrNull() ?: 0f) * oran).replace(",", "."),
                    fat = "%.1f".format((varsayilan.fat?.toFloatOrNull() ?: 0f) * oran).replace(",", ".")
                )
            }
            
            varsayilan
        } catch (e: Exception) {
            Log.e(TAG, "Porsiyon parse hatası: ${e.message}")
            null
        }
    }
}
