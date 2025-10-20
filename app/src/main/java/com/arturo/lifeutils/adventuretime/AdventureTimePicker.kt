package com.arturo.lifeutils.adventuretime

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import com.arturo.lifeutils.ui.theme.MainActivityViewModel
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdventureTimePicker(
    viewModel: MainActivityViewModel,
    timeInputState: TimePickerState
) {
    TimePickerDialog(
        title = {
            TimePickerDialogDefaults.Title(displayMode = TimePickerDisplayMode.Input)
        },
        onDismissRequest = { viewModel.showTimeInput(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timeInputState.hour)
                    cal.set(Calendar.MINUTE, timeInputState.minute)
                    cal.isLenient = false
                    viewModel.updateAdventureTime(cal.time)
                }
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.showTimeInput(false)
            }) { Text("Cancel") }
        },
        modeToggleButton = {},
    ) {
        TimeInput(state = timeInputState)
    }
}