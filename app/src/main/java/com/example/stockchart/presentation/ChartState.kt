package com.example.stockchart.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.stockchart.data.network.dto.BarInfoDto
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Suppress("UNCHECKED_CAST")
data class ChartState(
    val stockBars: List<BarInfoDto>,
    val componentUiWidth: Float = 0f,
    val scrolled: Float = 0f,
    val visibleNumberOfBars: Int = DEFAULT_NUMBER_OF_VISIBLE_BARS
) {
    val widthDistance: Float
        get() =
            componentUiWidth / visibleNumberOfBars

    val visibleStockBarsOnScreen: List<BarInfoDto>
        get() {
            val passedBy = (scrolled / widthDistance)
                .roundToInt()
                .coerceAtLeast(0)
            val endIndex = (passedBy + visibleNumberOfBars).coerceAtMost(stockBars.size - 1)
            return stockBars.subList(min(passedBy, endIndex), max(passedBy, endIndex)) // sometimes passedBy > endIndex
        }

    companion object {
        const val DEFAULT_NUMBER_OF_VISIBLE_BARS = 80
        const val DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS = 20
        const val DEFAULT_MAX_NUMBER_OF_VISIBLE_BARS = 1000

        val Saver: Saver<MutableState<ChartState>, Any> = listSaver(
            save = {
                val state = it.value
                listOf(
                    state.componentUiWidth,
                    state.scrolled,
                    state.visibleNumberOfBars,
                    state.stockBars
                )
            }, restore = {
                mutableStateOf(
                    ChartState(
                        componentUiWidth = it[0] as Float,
                        scrolled = it[1] as Float,
                        visibleNumberOfBars = it[2] as Int,
                        stockBars = it[3] as List<BarInfoDto>,
                    )
                )
            }
        )
    }
}

@Composable
fun rememberChartState(bars: List<BarInfoDto>) = rememberSaveable(saver = ChartState.Saver) {
    mutableStateOf(ChartState(bars))
}