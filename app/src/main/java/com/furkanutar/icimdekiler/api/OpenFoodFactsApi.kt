package com.furkanutar.icimdekiler.api

import com.furkanutar.icimdekiler.model.OFFYanit
import retrofit2.http.GET
import retrofit2.http.Path

interface OpenFoodFactsApi{
    @GET("api/v0/product/{barkodNo}.json")
    suspend fun getUrun(@Path("barkodNo") barkodNo: String): OFFYanit
}
