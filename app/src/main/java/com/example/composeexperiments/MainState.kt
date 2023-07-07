package com.example.composeexperiments

sealed interface MainViewState {
    object Loading : MainViewState
    data class Nominal(
        val single: ComponentData,
        val double: ComponentData,
        val triple: ComponentData
    ) : MainViewState
}