package com.example.stockchart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import com.example.stockchart.data.network.dto.BarInfoDto
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

private const val DEFAULT_NUMBER_OF_VISIBLE_BARS = 80
private const val DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS = 20
private const val DEFAULT_MAX_NUMBER_OF_VISIBLE_BARS = 1000

@Composable
fun Chart(stockBars: List<BarInfoDto>, modifier: Modifier = Modifier) {
    var visibleNumberOfBars by remember {
        mutableStateOf(DEFAULT_NUMBER_OF_VISIBLE_BARS)
    }

    var componentUiSize by remember {
        mutableStateOf(Size(0f, 0f))
    }

    var pxPerDollar by remember {
        mutableStateOf(0f)
    }

    val widthDistance by remember {
        derivedStateOf {
            componentUiSize.width / visibleNumberOfBars
        }
    }

    var scrolled by remember {
        mutableStateOf(0f)
    }

    val visibleStockBarsOnScreen by remember {
        derivedStateOf {
            val passedBy = (scrolled / widthDistance)
                .roundToInt()
                .coerceAtLeast(0)
            val endIndex = (passedBy + visibleNumberOfBars).coerceAtMost(stockBars.size - 1)
            stockBars.subList(passedBy, endIndex)
        }
    }

    val transform = TransformableState(onTransformation = { zoomChange, panChange, _ ->
        visibleNumberOfBars = (visibleNumberOfBars / zoomChange)
            .roundToInt()
            .coerceIn(DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS, stockBars.size)

        scrolled =
            (scrolled + panChange.x).coerceIn(
                0f,
                abs(widthDistance * stockBars.size - componentUiSize.width)
            )
    })

    Canvas(modifier = modifier.transformable(transform)) {
        componentUiSize = size
        val maxCost = visibleStockBarsOnScreen.maxOf { it.highest }
        val minCost = visibleStockBarsOnScreen.minOf { it.lowest }
        pxPerDollar = size.height / (maxCost - minCost)
        translate(left = scrolled) {
            stockBars.forEachIndexed { index, barInfoDto ->
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

                drawRect(
                    if (barInfoDto.open < barInfoDto.close) Color.Green else Color.Red,
                    topLeft = Offset(
                        size.width - index * widthDistance - widthDistance / 4,
                        size.height - pxPerDollar * max(
                            barInfoDto.open,
                            barInfoDto.close
                        ) + pxPerDollar * minCost
                    ),
                    size = Size(
                        width = widthDistance / 2,
                        height = pxPerDollar * abs(barInfoDto.open - barInfoDto.close)
                    )
                )
            }
        }
    }
}