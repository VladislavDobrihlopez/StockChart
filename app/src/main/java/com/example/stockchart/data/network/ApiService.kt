package com.example.stockchart.data.network

import com.example.stockchart.Configuration
import com.example.stockchart.data.network.dto.ResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("aggs/ticker/{stock_name}/range/{time_frame}/2022-01-09/2023-01-09?adjusted=true&sort=desc&limit=50000&apiKey=${Configuration.API_KEY}")
    suspend fun getStockBars(
        @Path("stock_name") name: String,
        @Path("time_frame") timeFrame: String
    ): ResponseDto
}