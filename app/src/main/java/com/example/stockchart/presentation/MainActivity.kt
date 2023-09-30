package com.example.stockchart.presentation

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockchart.data.network.ApiFactory
import com.example.stockchart.presentation.ui.theme.StockChartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockChartTheme {
                val context = LocalContext.current
                val viewModel: MainActivityViewModel = viewModel()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    Button(onClick = { viewModel.onEvent() }) {
                        Text(text = "Request data")
                    }

                    when (val state = viewModel.screenState.collectAsState().value) {
                        is ScreenState.Content -> {
                            Log.d("MainActivity", "onCreate: ${state.bars}")
                            Chart(
                                stockBars = state.bars,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black)
                            )
                        }

                        is ScreenState.Failure -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                        }

                        ScreenState.Initial -> {

                        }
                    }
                }
            }
        }
    }
}
