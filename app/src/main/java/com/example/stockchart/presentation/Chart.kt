package com.example.stockchart.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.stockchart.data.network.dto.BarInfoDto

@Composable
fun Chart(stockBars: List<BarInfoDto>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val widthDistance = size.width / stockBars.size
        val maxCost = stockBars.maxOf { it.highest }
        val minCost = stockBars.minOf { it.lowest }
        val pxPerDollar = size.height / (maxCost - minCost)
        stockBars.forEachIndexed { index, barInfoDto ->
            drawLine(
                color = Color.Yellow,
                start = Offset(index * widthDistance, size.height - pxPerDollar * barInfoDto.lowest + pxPerDollar * minCost),
                end = Offset(index * widthDistance, size.height - pxPerDollar * barInfoDto.highest + pxPerDollar * minCost),
            )
        }
    }
}