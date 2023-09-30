package com.example.stockchart.data.network

import com.example.stockchart.Configuration
import com.example.stockchart.data.network.dto.ResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("aggs/ticker/{stock_name}/range/1/hour/2022-01-09/2023-01-09?adjusted=true&sort=asc&limit=50000&apiKey=${Configuration.API_KEY}")
//    @GET("aggs/ticker/AAPL/range/1/hour/2022-01-09/2023-01-09?adjusted=true&sort=asc&limit=50000&apiKey=Is6w5efIZ0AYP5cf1wBPQCo4Fo4LXobp")
    suspend fun getStockBars(@Path("stock_name") name: String): ResponseDto
}