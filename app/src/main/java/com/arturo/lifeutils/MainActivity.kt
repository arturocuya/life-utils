package com.arturo.lifeutils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arturo.lifeutils.adventuretime.AdventureTimePicker
import com.arturo.lifeutils.ui.theme.LifeUtilsTheme
import com.arturo.lifeutils.ui.theme.MainActivityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = viewModel.uiState.collectAsState().value

            LifeUtilsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        innerPadding = innerPadding,
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    innerPadding: PaddingValues,
    state: MainActivityViewModel.MainActivityState,
    viewModel: MainActivityViewModel,
) {
    val timeFormatter = remember {
        SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        )
    }
    val timeInputState = rememberTimePickerState()

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 32.dp)
    ) {
        Text(
            text = "What's today's adventure?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.adventureTitle,
                onValueChange = {
                    viewModel.updateAdventureTitle(it)
                },
                placeholder = { Text("Enter something") }
            )

            Button(
                onClick = { viewModel.showTimeInput(true) },
                shape = RoundedCornerShape(4.dp),
                enabled = state.adventureTitle.isNotEmpty(),
            ) {
                Text("Set time")
            }
        }

        if (state.showTimeInput) {
            AdventureTimePicker(
                viewModel = viewModel,
                timeInputState = timeInputState
            )
        }

        state.adventureTime?.let {
            val timeString = timeFormatter.format(it)
            Text(
                text = "Adventure set for $timeString",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text("Prep Missions:", style = MaterialTheme.typography.titleMedium)

            val startingTime = state.prepMissions.fold(state.adventureTime) { currentDate, mission ->
                Date(currentDate.time - mission.duration * 60 * 1000L)
            }
            Text("You should start prepping at: ${timeFormatter.format(startingTime)}")
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.prepMissions.forEach { mission ->
                    Text(
                        "- ${mission.name} (${mission.duration}')",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                var textState by remember { mutableStateOf("") }
                var durationState by remember { mutableStateOf("") }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { value ->
                            textState = value
                        },
                        placeholder = { Text("Prep Mission #${state.prepMissions.size + 1} name") }
                    )
                    OutlinedTextField(
                        value = durationState,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { value ->
                            durationState = value
                        },
                        placeholder = { Text("Prep Mission #${state.prepMissions.size + 1} duration") }
                    )
                    Button(
                        onClick = {
                            viewModel.addMission(
                                MainActivityViewModel.PrepMission(
                                    name = textState,
                                    duration = durationState.toInt()
                                )
                            )
                            textState = ""
                            durationState = ""
                        },
                        shape = RoundedCornerShape(4.dp),
                        enabled = textState.isNotEmpty(),
                    ) {
                        Text("Add mission")
                    }
                }
            }
        }
    }
}