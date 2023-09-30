package com.example.stockchart.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockchart.data.network.ApiFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Initial)
    val screenState = _screenState.asStateFlow()

    private val service = ApiFactory.apiService()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("MainActivityViewModel", "Exception caught: ${throwable.message}")
    }

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