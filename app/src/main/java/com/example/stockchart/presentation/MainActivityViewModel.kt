package com.example.stockchart.presentation

import android.net.ConnectivityManager
import android.net.Network
import android.util.Log
import androidx.core.net.ConnectivityManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockchart.data.network.ApiFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.Socket

class MainActivityViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val service = ApiFactory.apiService()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MainActivityViewModel", "Exception caught: ${throwable.message}")
    }

    private val scope = CoroutineScope(Job())
    private val flow = flow<Int> {
        while (true) {
            delay(2000)
            emit((Math.random() * 10).toInt() % 10)
        }
    }
    private val viewModelFlow = flow
        .cancellable()
        .onStart {
            scope.launch {
                delay(15500)
                scope.cancel()
            }
        }
        .onCompletion {
            if (it is CancellationException) {
                Log.d("TEST_FLOW", "cancelled")
            }
        }
        .onEach {
            Log.d("TEST_FLOW", it.toString())
        }
        .launchIn(scope)

    fun onEvent() {

        viewModelScope.launch(exceptionHandler) {
            val result = service.getStockBars("AAPL")
            if (result.status == "OK") {
                _screenState.value = ScreenState.Content(result.results)
            } else {
                _screenState.value = ScreenState.Failure("Failure. Try again in a minute")
            }
        }
    }
}