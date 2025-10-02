package com.mstf.segmentedprogressring

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class MainViewModel : ViewModel() {

    private var _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun addProgress() {
        val updatedStories = _uiState.value.progressList.toMutableList()
        updatedStories.add(Random.nextFloat())
        _uiState.update {
            uiState.value.copy(
                progressList = updatedStories
            )
        }
    }

    fun updateProgress(index: Int, value: Float) {
        val updatedProgressList = _uiState.value.progressList.toMutableList()
        updatedProgressList[index] = value
        _uiState.update {
            uiState.value.copy(
                progressList = updatedProgressList
            )
        }
    }
}

data class MainUiState(
    val progressList: List<Float> = arrayListOf(),
)