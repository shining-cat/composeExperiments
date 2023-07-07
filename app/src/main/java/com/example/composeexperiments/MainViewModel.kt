package com.example.composeexperiments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _screenViewState = MutableStateFlow<MainViewState>(MainViewState.Loading)
    val screenViewState = _screenViewState.asStateFlow()
    private val _tickingState = MutableStateFlow<Boolean>(false)
    val tickingState = _tickingState.asStateFlow()

    private var ticker: Job? = null
    private var firstTicker: Int = 0
    private var secondTicker: Int = 0
    private var thirdTicker: Int = 0

    fun start() {
        ticker = viewModelScope.launch {
            _tickingState.emit(true)
            while (true) {
                tick()
                delay(1000)
            }
        }
    }

    fun pause() {
        viewModelScope.launch {
            _tickingState.emit(false)
        }
        ticker?.cancel()
        ticker = null
    }

    fun stop() {
        pause()
        firstTicker = 0
        secondTicker = 0
        thirdTicker = 0
        emitCurrentTickingStates()
    }

    fun manualTick() {
        tick()
    }

    private fun tick() {
        firstTicker++
        if (firstTicker.minus(secondTicker) == 3) secondTicker += 3
        if (firstTicker.minus(thirdTicker) == 6) thirdTicker += 6
        emitCurrentTickingStates()
    }

    private fun emitCurrentTickingStates(){
        viewModelScope.launch() {
            _screenViewState.emit(
                MainViewState.Nominal(
                    single = ComponentData(displayValue = firstTicker.toString()),
                    double = ComponentData(displayValue = secondTicker.toString()),
                    triple = ComponentData(displayValue = thirdTicker.toString())
                )
            )
        }
    }
}