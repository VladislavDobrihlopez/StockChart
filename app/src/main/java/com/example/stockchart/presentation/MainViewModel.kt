package com.example.stockchart.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockchart.data.network.ApiFactory
import com.example.stockchart.data.providePath
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val screenState = _screenState.asStateFlow()
    private var previousSucceededState: ScreenState? = null

    private val service = ApiFactory.apiService()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MainActivityViewModel", "Exception caught: ${throwable.message}")
        _screenState.value =
            previousSucceededState ?: ScreenState.Failure(throwable.message.toString())
    }

    init {
        loadNewData(TimeFrame.H_1)
    }

    fun onEvent(event: ChartEvents) {
        when (event) {
            is ChartEvents.OnChangeTimeFrame -> onChangeTimeFrame(event.timeFrame)
        }
    }

    private fun onChangeTimeFrame(timeFrame: TimeFrame) {
        loadNewData(timeFrame)
    }

    private fun loadNewData(timeFrame: TimeFrame) {
        _screenState.value = ScreenState.Loading
        viewModelScope.launch(exceptionHandler) {
            val result = service.getStockBars("AAPL", timeFrame.providePath())
            if (result.status == "OK") {
                val successScreenState = ScreenState.Content(result.results, timeFrame)
                previousSucceededState = successScreenState
                _screenState.value = successScreenState
            } else {
                _screenState.value = ScreenState.Failure("Failure. Try again in a minute")
            }
        }
    }
}

sealed class ChartEvents {
    data class OnChangeTimeFrame(val timeFrame: TimeFrame) : ChartEvents()
}