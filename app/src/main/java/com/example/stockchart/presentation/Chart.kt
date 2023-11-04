package com.example.stockchart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockchart.data.network.dto.BarInfoDto
import com.example.stockchart.presentation.ChartState.Companion.DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt


@OptIn(ExperimentalTextApi::class)
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

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier
        .transformable(transform)
        .onSizeChanged {
            chartState =
                chartState.copy(componentUiWidth = it.width.toFloat())
        }
        .clipToBounds()
        .padding(vertical = 24.dp)
    ) {
        val maxCost = chartState.visibleStockBarsOnScreen.maxOf { it.highest }
        val minCost = chartState.visibleStockBarsOnScreen.minOf { it.lowest }
        val pxPerDollar = size.height / (maxCost - minCost)

        chartState.visibleStockBarsOnScreen.firstOrNull()?.let {
            drawBoundaryPrices(
                maxCost,
                minCost,
                it.close, // we draw right to left therefore first item is the latest
                pxPerDollar,
                textMeasurer
            )
        }

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

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBoundaryPrices(
    maxPriceEver: Float,
    minPriceEver: Float,
    lastPrice: Float,
    pxPerDollar: Float,
    textMeasurer: TextMeasurer
) {
    // min
    drawDashedLineWithText(
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        text = minPriceEver.toString(),
        textMeasurer = textMeasurer
    )

    // max
    drawDashedLineWithText(
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        text = maxPriceEver.toString(),
        textMeasurer = textMeasurer
    )

    // last
    drawDashedLineWithText(
        start = Offset(0f, size.height - pxPerDollar * (lastPrice - minPriceEver)),
        end = Offset(size.width, size.height - pxPerDollar * (lastPrice - minPriceEver)),
        text = lastPrice.toString(),
        textMeasurer = textMeasurer
    )
}

private fun DrawScope.drawDashedLine(
    start: Offset,
    end: Offset,
) {
    drawLine(
        color = Color.White,
        start = start,
        strokeWidth = 3.dp.toPx(),
        end = end,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(
                7.dp.toPx(),
                3.dp.toPx()
            )
        )
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDashedLineWithText(
    text: String,
    textMeasurer: TextMeasurer,
    start: Offset,
    end: Offset,
) {
    val textResult =
        textMeasurer.measure(
            text,
            TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W700, color = Color.White)
        )
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(size.width - textResult.size.width, end.y)
    )
    drawDashedLine(start = start, end = end)
}