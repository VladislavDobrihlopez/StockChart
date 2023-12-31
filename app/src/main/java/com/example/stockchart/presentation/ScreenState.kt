package com.example.stockchart.presentation

import com.example.stockchart.data.network.dto.BarInfoDto

sealed class ScreenState {
    object Initial : ScreenState()
    data class Failure(val message: String) : ScreenState()
    data class Content(
        val bars: List<BarInfoDto> = emptyList(),
        val selectedTimeFrame: TimeFrame = TimeFrame.H_1
    ) : ScreenState()

    object Loading : ScreenState()
}
