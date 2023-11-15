package com.example.stockchart.data

import com.example.stockchart.presentation.TimeFrame

fun TimeFrame.providePath(): String {
    return when (this) {
        TimeFrame.M_5 -> "5/minute"
        TimeFrame.M_15 -> "15/minute"
        TimeFrame.M_30 -> "30/minute"
        TimeFrame.H_1 -> "1/hour"
    }
}