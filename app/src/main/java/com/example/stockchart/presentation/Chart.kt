package com.example.stockchart.presentation

import android.icu.util.Calendar
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockchart.data.network.dto.BarInfoDto
import com.example.stockchart.presentation.ChartState.Companion.DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS
import java.util.Calendar.SHORT_FORMAT
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt


@Composable
fun Chart(modifier: Modifier = Modifier, viewModel: MainActivityViewModel = viewModel()) {
    val context = LocalContext.current
    when (val state =
        viewModel.screenState.collectAsState().value) { // might be changes to collectAsStateWithLifecycle
        is ScreenState.Content -> {
            val chartState = rememberChartState(state.bars)
            Box(
                modifier = modifier
                    .background(Color.Black),
            ) {
                CoreChart(
                    state = chartState,
                    selectedTimeFrame = state.selectedTimeFrame,
                    onStateTransformation = { bars, scroll ->
                        chartState.value = chartState.value.copy(
                            visibleNumberOfBars = bars,
                            scrolled = scroll
                        )
                    },
                    onChangeSize = {
                        chartState.value = chartState.value.copy(
                            componentUiWidth = it.width.toFloat(),
                            height = it.height.toFloat()
                        )
                    })

                TimeFrames(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .zIndex(5f),
                    onClick = {
                        viewModel.onEvent(ChartEvents.OnChangeTimeFrame(it))
                    },
                    selectedTimeFrame = state.selectedTimeFrame
                )

                state.bars.firstOrNull()?.let {
                    PricesText(
                        state = chartState,
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .padding(vertical = 32.dp)
                            .zIndex(1f),
                        lastPrice = it.close,
                    )
                }
            }
        }

        is ScreenState.Failure -> {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
        }

        ScreenState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ScreenState.Initial -> {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFrames(
    modifier: Modifier = Modifier,
    selectedTimeFrame: TimeFrame,
    onClick: (TimeFrame) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeFrame.values().forEach { timeFrame ->
            val isCurrentSelected = timeFrame == selectedTimeFrame
            val (containerColor, labelColor) =
                if (isCurrentSelected) {
                    Color.White to Color.Black
                } else {
                    Color.Black to Color.White
                }

            AssistChip(
                onClick = {
                    onClick(timeFrame)
                },
                label = {
                    Text(text = timeFrame.name)
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = containerColor,
                    labelColor = labelColor
                )
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun CoreChart(
    state: State<ChartState>,
    selectedTimeFrame: TimeFrame,
    onStateTransformation: (Int, Float) -> Unit,
    onChangeSize: (IntSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val chartState = state.value

    val transform = rememberTransformableState(onTransformation = { zoomChange, panChange, _ ->
        val newVisibleNumberOfBars = (chartState.visibleNumberOfBars / zoomChange)
            .roundToInt()
            .coerceIn(DEFAULT_MIN_NUMBER_OF_VISIBLE_BARS, chartState.stockBars.size)

        val newScrolledPosition = (chartState.scrolled + panChange.x)
            .coerceIn(
                0f,
                abs(chartState.widthDistance * chartState.stockBars.size - chartState.componentUiWidth)
            )
        onStateTransformation(newVisibleNumberOfBars, newScrolledPosition)
    })

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .transformable(transform)
            .onSizeChanged {
                onChangeSize(it)
            }
            .clipToBounds()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        val minCost = chartState.minCost
        val pxPerDollar = chartState.pxPerDollar

        translate(left = chartState.scrolled) {
            chartState.stockBars.forEachIndexed { index, barInfoDto ->
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

                drawDelimiters(
                    timeFrame = selectedTimeFrame,
                    bar = barInfoDto,
                    nextBar = if (index < chartState.stockBars.size - 1) chartState.stockBars[index + 1] else null,
                    textMeasurer = textMeasurer,
                    offsetX = size.width - index * chartState.widthDistance,
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun PricesText(
    state: State<ChartState>,
    lastPrice: Float,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .padding(vertical = 32.dp)
    ) {
        drawBoundaryPrices(
            maxPriceEver = state.value.maxCost,
            minPriceEver = state.value.minCost,
            lastPrice = lastPrice,
            pxPerDollar = state.value.pxPerDollar,
            textMeasurer = measurer
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDelimiters(
    timeFrame: TimeFrame,
    bar: BarInfoDto,
    nextBar: BarInfoDto?,
    textMeasurer: TextMeasurer,
    offsetX: Float
) {

    val calendar = bar.calendar
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hours = calendar.get(Calendar.HOUR_OF_DAY)
    val minutes = calendar.get(Calendar.MINUTE)

    val shouldDrawDelimiter = when (timeFrame) {
        TimeFrame.M_5 -> {
            minutes == 0
        }

        TimeFrame.M_15 -> {
            minutes == 0 && hours % 2 == 0
        }

        TimeFrame.M_30, TimeFrame.H_1 -> {
            val nextBarDay = nextBar?.calendar?.get(Calendar.DAY_OF_MONTH)
            nextBarDay != day
        }
    }

    if (!shouldDrawDelimiter) return

    val nameOfMonth =
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, SHORT_FORMAT, Locale.getDefault())

    val text = when (timeFrame) {
        TimeFrame.M_5, TimeFrame.M_15 -> {
            String.format("%02d:00", hours)
        }

        TimeFrame.M_30, TimeFrame.H_1 -> {
            String.format("%s %s", day, nameOfMonth)
        }
    }

    drawLine(
        color = Color.White.copy(alpha = 0.5f),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX, size.height),
        strokeWidth = 2f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
    )

    val textResult =
        textMeasurer.measure(
            text,
            TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W700, color = Color.White)
        )

    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(
            offsetX - textResult.size.width / 2,
            size.height - textResult.size.height * 1.5f
        )
    )
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
        topLeft = Offset(size.width - textResult.size.width - 5.dp.toPx(), end.y)
    )
    drawDashedLine(start = start, end = end)
}