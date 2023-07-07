package com.example.composeexperiments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.composeexperiments.ui.theme.ComposeExperimentsTheme
import kotlin.random.Random

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val viewState = viewModel.screenViewState.collectAsState().value
    val tickingState = viewModel.tickingState.collectAsState().value
    val topRememberedValue = "Remembered at the top level"
    MainComposable(
        onStart = { viewModel.start() },
        onPause = { viewModel.pause() },
        onStop = { viewModel.stop() },
        onManualTick = { viewModel.manualTick() },
        viewState = viewState,
        topRememberedValue = topRememberedValue,
        tickingState = tickingState
    )
}

@Composable
fun MainComposable(
    onStart: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onManualTick: () -> Unit = {},
    viewState: MainViewState,
    topRememberedValue: String,
    tickingState: Boolean
) {
    val rememberedValue = "Remembered one level above"
    when (viewState) {
        MainViewState.Loading -> WaitingComposable(onStart = onStart)
        is MainViewState.Nominal -> NominalComposable(
            onStart = onStart,
            onPause = onPause,
            onStop = onStop,
            onManualTick = onManualTick,
            viewState = viewState,
            topRememberedValue = topRememberedValue,
            valueRememberedAbove = rememberedValue,
            tickingState = tickingState
        )
    }
}

@Composable
fun WaitingComposable(onStart: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(text = "Waiting...")
            Button(onClick = onStart) {
                Text(text = "LAUNCH")
            }
        }
    }
}

@Composable
fun NominalComposable(
    onStart: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onManualTick: () -> Unit = {},
    viewState: MainViewState.Nominal,
    topRememberedValue: String,
    valueRememberedAbove: String,
    tickingState: Boolean
) {
    val valueRememberedHere = remember{"Remembered on the same level"}
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        //all these Texts are recomposed every time there's a change in viewState even though they have nothing to do with what it contains
        Text(
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = topRememberedValue,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Text(
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = valueRememberedAbove,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Text(
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = valueRememberedHere,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Text(
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = "this is not remembered",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        // doing the same as above but simply nesting Text in a Composable => these never recompose!
        JustAText(label = topRememberedValue)
        JustAText(label = valueRememberedAbove)
        JustAText(label = valueRememberedHere)
        JustAText(label = "this is not remembered")
        //
        Component(//this recomposes as expected every time viewState.single changes
            label = "I update every time:",
            inputData = viewState.single
        )
        Component(//this recomposes as expected only when viewState.double changes, no need for any remembering
            label = "I update every 3rd time:",
            inputData = viewState.double
        )
        Component(//this recomposes as expected only when viewState.triple changes, no need for any remembering
            label = "I update every 6th time:",
            inputData = viewState.triple
        )
        Row() {
            Button(onClick = onStart, enabled = !tickingState) {
                Text(text = "RESTART")
            }
            Button(onClick = onPause, enabled = tickingState) {
                Text(text = "PAUSE")
            }
            Button(onClick = onStop, enabled = tickingState) {
                Text(text = "STOP")
            }
        }
        Button(onClick = onManualTick) {
            Text(text = "MANUAL INCREASE")
        }

    }
}

/*
*
* composable level 1 composes:
*   -> composes all children, remember fields are evaluated and stored
* composable level 1 REcomposes:
*   -> remember fields are NOT REevaluated (the point of remember is to survive recomposition)
*   -> REcomposes all contained native Composables, even if their provided parameters have NOT changed
*   -> nested children Composables will recompose also ONLY IF the parameters they are provided change (not changing it could be achieved with remember or without)
*
* */
@Composable
fun JustAText(label: String){ //this Composable only recomposes when a different label is provided
    Text(
        modifier = Modifier.background(getRandomColor()),
        text = label,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp
    )
}

@Composable
fun Component(
    label: String,
    inputData: ComponentData
) {
    Row (modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly){
        JustAText(label = label)//this is never recomposed, because label never changes
        ///////////////
        val rememberedDisplayValue = remember{inputData.displayValue}
        Text( // this recomposes every time inputData changes, but the displayValue never changes, because remember will return the value calculated on the first composition on every recomposition
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = rememberedDisplayValue,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        JustAText(label = rememberedDisplayValue) //this won't ever be recomposed, even though inputData.displayValue is changing, because remember will return the value calculated on the first composition on every recomposition
        Text( // this recomposes as expected every time inputData.displayValue changes, as it depends directly on its content
            modifier = Modifier.border(2.dp, getRandomColor()),
            text = rememberedDisplayValue,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        JustAText(label = inputData.displayValue) // this recomposes as expected every time inputData.displayValue changes, as it depends directly on its content
    }
}

fun getRandomColor() = Color(
    red = Random.nextInt(256),
    green = Random.nextInt(256),
    blue = Random.nextInt(256),
    alpha = 255
)

@Preview
@Composable
private fun ChoiceDialogPreview() {
    ComposeExperimentsTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainComposable(
                viewState = MainViewState.Nominal(
                    single = ComponentData(displayValue = "1"),
                    double = ComponentData(displayValue = "3"),
                    triple = ComponentData(displayValue = "6")
                ),
                topRememberedValue = "topRememberedValue",
                tickingState = true
            )
        }
    }
}
