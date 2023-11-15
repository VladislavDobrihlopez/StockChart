package com.example.stockchart.data.network.dto

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName
import java.util.Calendar
import java.util.Date

@Immutable
data class BarInfoDto(
    @SerializedName("o") val open: Float,
    @SerializedName("c") val close: Float,
    @SerializedName("h") val highest: Float,
    @SerializedName("l") val lowest: Float,
    @SerializedName("t") val timestamp: Long
) {
    val calendar: Calendar
        get() = Calendar.getInstance().apply {
            time = Date(timestamp)
        }
}
