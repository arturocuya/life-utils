package com.arturo.lifeutils.ui.theme

import androidx.lifecycle.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

class MainActivityViewModel : ViewModel() {
    data class MainActivityState(
        val adventureTitle: String = "",
        val showTimeInput: Boolean = false,
        val adventureTime: Date? = null,
        val prepMissions: ImmutableList<PrepMission> = persistentListOf()
    )

    data class PrepMission(
        val name: String,
        val duration: Int,
    )

    private val _uiState = MutableStateFlow(MainActivityState())
    val uiState: StateFlow<MainActivityState> = _uiState.asStateFlow()

    fun updateAdventureTitle(newTitle: String) {
        _uiState.value = _uiState.value.copy(adventureTitle = newTitle)
    }

    fun showTimeInput(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTimeInput = show)
    }

    fun updateAdventureTime(newTime: Date) {
        _uiState.value = _uiState.value.copy(adventureTime = newTime, showTimeInput = false)
    }

    fun addMission(prepMission: PrepMission) {
        val list = _uiState.value.prepMissions.toMutableList()
        list.add(prepMission)
        _uiState.value = _uiState.value.copy(prepMissions = list.toImmutableList())
    }
}


