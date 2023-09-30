package com.example.stockchart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import com.example.stockchart.data.network.dto.BarInfoDto
import com.example.stockchart.presentation.ChartState.Companion.DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun Chart(stockBars: List<BarInfoDto>, modifier: Modifier = Modifier) {
    var chartState by rememberChartState(stockBars)

    val transform = TransformableState(onTransformation = { zoomChange, panChange, _ ->
        val newVisibleNumberOfBars = (chartState.visibleNumberOfBars / zoomChange)
            .roundToInt()
            .coerceIn(DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS, stockBars.size)

        val newScrolledPosition = (chartState.scrolled + panChange.x)
            .coerceIn(
                0f,
                abs(chartState.widthDistance * stockBars.size - chartState.componentUiWidth)
            )

        chartState = chartState.copy(
            visibleNumberOfBars = newVisibleNumberOfBars,
            scrolled = newScrolledPosition
        )
    })

    Canvas(modifier = modifier
        .transformable(transform)
        .onSizeChanged {
            chartState =
                chartState.copy(componentUiWidth = it.width.toFloat())
        }) {
        val maxCost = chartState.visibleStockBarsOnScreen.maxOf { it.highest }
        val minCost = chartState.visibleStockBarsOnScreen.minOf { it.lowest }
        val pxPerDollar = size.height / (maxCost - minCost)
        translate(left = chartState.scrolled) {
            stockBars.forEachIndexed { index, barInfoDto ->
                drawLine(
                    color = Color.Yellow,
                    start = Offset(
                        size.width - index * chartState.widthDistance, // starting drawing from the right
                        size.height - pxPerDollar * barInfoDto.lowest + pxPerDollar * minCost
                    ),
                    end = Offset(
                        size.width - index * chartState.widthDistance,
                        size.height - pxPerDollar * barInfoDto.highest + pxPerDollar * minCost
                    ),
                )

                drawRect(
                    if (barInfoDto.open < barInfoDto.close) Color.Green else Color.Red,
                    topLeft = Offset(
                        size.width - index * chartState.widthDistance - chartState.widthDistance / 4,
                        size.height - pxPerDollar * max(
                            barInfoDto.open,
                            barInfoDto.close
                        ) + pxPerDollar * minCost
                    ),
                    size = Size(
                        width = chartState.widthDistance / 2,
                        height = pxPerDollar * abs(barInfoDto.open - barInfoDto.close)
                    )
                )
            }
        }
    }
}