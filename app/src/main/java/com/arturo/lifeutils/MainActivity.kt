package com.arturo.lifeutils

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.arturo.lifeutils.adventuretime.AdventureTimePicker
import com.arturo.lifeutils.ui.theme.LifeUtilsTheme
import com.arturo.lifeutils.ui.theme.MainActivityViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = viewModel.uiState.collectAsState().value

            LifeUtilsTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { innerPadding ->
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
    val currentCalendar = remember { Calendar.getInstance() }
    val timeInputState = rememberTimePickerState(
        initialHour = (currentCalendar.get(Calendar.HOUR_OF_DAY) + 1) % 24,
        initialMinute = currentCalendar.get(Calendar.MINUTE),
    )
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.safeContent)
    ) {
        Text(
            text = "What's today's adventure?",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.adventureTitle,
                onValueChange = {
                    viewModel.updateAdventureTitle(it)
                },
                placeholder = { Text("Enter something") }
            )

            IconButton(
                modifier = Modifier.size(TextFieldDefaults.MinHeight),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { viewModel.showTimeInput(true) },
                shape = RoundedCornerShape(4.dp),
                enabled = state.adventureTitle.isNotEmpty(),
            ) {
                Icon(
                    painter = painterResource(R.drawable.calendar_clock_24px),
                    contentDescription = "Set date and time"
                )
            }
        }

        if (state.showTimeInput) {
            AdventureTimePicker(
                viewModel = viewModel,
                timeInputState = timeInputState
            )
        }

        state.adventureTime?.let { adventureTime ->
            val timeString = timeFormatter.format(adventureTime)
            Text(
                text = "Adventure set for $timeString",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            Text("Prep Missions:", style = MaterialTheme.typography.titleMedium)

            val startingTime =
                state.prepMissions.fold(state.adventureTime) { currentDate, mission ->
                    Date(currentDate.time - mission.duration * 60 * 1000L)
                }
            Text("You should start prepping at: ${timeFormatter.format(startingTime)}")
            Spacer(Modifier.height(16.dp))

            val hapticFeedback = LocalHapticFeedback.current
            val lazyColumnState = rememberLazyListState()
            val reorderableLazyListState =
                rememberReorderableLazyListState(lazyColumnState) { from, to ->
                    viewModel.reorderMissions(from.index, to.index)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                }

            var textState by remember { mutableStateOf("") }
            var durationState by remember { mutableStateOf("") }
            var shouldRequestFocus by remember { mutableStateOf(false) }

            val coroutineScope = rememberCoroutineScope()
            val nameFocusRequester = remember { FocusRequester() }
            val durationFocusRequester = remember { FocusRequester() }

            LaunchedEffect(shouldRequestFocus) {
                if (shouldRequestFocus) {
                    nameFocusRequester.requestFocus()
                    shouldRequestFocus = false
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyColumnState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.prepMissions, key = { it.name }) { mission ->
                    ReorderableItem(
                        reorderableLazyListState,
                        key = mission.name
                    ) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)
                        Surface(shadowElevation = elevation) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(
                                                HapticFeedbackType.GestureThresholdActivate
                                            )
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(
                                                HapticFeedbackType.GestureEnd
                                            )
                                        },
                                    ),
                                    onClick = {}
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.drag_handle_24px),
                                        contentDescription = "Drag Handle"
                                    )
                                }
                                Text(
                                    "${mission.name} (${mission.duration}')",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = textState,
                            onValueChange = { value ->
                                textState = value
                            },
                            placeholder = { Text("Prep Mission #${state.prepMissions.size + 1} name") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    durationFocusRequester.requestFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(nameFocusRequester)
                        )
                        OutlinedTextField(
                            value = durationState,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    viewModel.addMission(
                                        MainActivityViewModel.PrepMission(
                                            name = textState,
                                            duration = durationState.toInt()
                                        )
                                    )
                                    textState = ""
                                    durationState = ""
                                    shouldRequestFocus = true
                                }
                            ),
                            onValueChange = { value ->
                                durationState = value
                            },
                            placeholder = { Text("Prep Mission #${state.prepMissions.size + 1} duration") },
                            modifier = Modifier
                                .focusRequester(durationFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            lazyColumnState.animateScrollToItem(state.prepMissions.size + 1)
                                        }
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
