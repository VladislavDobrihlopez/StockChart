package com.example.stockchart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.stockchart.data.network.dto.BarInfoDto
import kotlin.math.roundToInt

private const val DEFAULT_NUMBER_OF_VISIBLE_BARS = 80
private const val DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS = 20
private const val DEFAULT_MAX_NUMBER_OF_VISIBLE_BARS = 1000

@Composable
fun Chart(stockBars: List<BarInfoDto>, modifier: Modifier = Modifier) {
    var visibleNumberOfBars by remember {
        mutableStateOf(DEFAULT_NUMBER_OF_VISIBLE_BARS)
    }

    val transform = TransformableState(onTransformation = { zoomChange, _, _ ->
        visibleNumberOfBars = (visibleNumberOfBars / zoomChange)
            .roundToInt()
            .coerceIn(DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS, stockBars.size)
    })

    Canvas(modifier = modifier.transformable(transform)) {
        val widthDistance = size.width / visibleNumberOfBars
        val maxCost = stockBars.maxOf { it.highest }
        val minCost = stockBars.minOf { it.lowest }
        val pxPerDollar = size.height / (maxCost - minCost)
        stockBars.take(visibleNumberOfBars).forEachIndexed { index, barInfoDto ->
            drawLine(
                color = Color.Yellow,
                start = Offset(
                    size.width - index * widthDistance, // starting drawing from the right
                    size.height - pxPerDollar * barInfoDto.lowest + pxPerDollar * minCost
                ),
                end = Offset(
                    size.width - index * widthDistance,
                    size.height - pxPerDollar * barInfoDto.highest + pxPerDollar * minCost
                ),
            )
        }
    }
}