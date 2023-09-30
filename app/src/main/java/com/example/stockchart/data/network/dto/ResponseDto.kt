package com.example.stockchart.data.network.dto

import com.google.gson.annotations.SerializedName

data class ResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("results") val results: List<BarInfoDto>
)