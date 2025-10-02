package com.mstf.segmentedprogressring

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private var _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun addProgress() {
        val updatedStories = _uiState.value.progressList.toMutableList()
        updatedStories.add(0f) // start empty
        _uiState.update {
            it.copy(
                progressList = updatedStories,
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

    fun updateTotalProgress(value: Float) {
        val segmentCount = _uiState.value.progressList.size
        if (segmentCount == 0) return

        val fullSegments = value.toInt()
        val partialProgress = value - fullSegments

        val newList = MutableList(segmentCount) { 0f }

        for (i in 0 until fullSegments.coerceAtMost(segmentCount)) {
            newList[i] = 1f // completed
        }
        if (fullSegments < segmentCount) {
            newList[fullSegments] = partialProgress
        }

        _uiState.update {
            it.copy(
                progressList = newList,
                totalProgress = value
            )
        }
    }

    fun toggleControlCheckbox() {
        _uiState.update {
            uiState.value.copy(
                controlAllSegmentsWithOneSlider = !uiState.value.controlAllSegmentsWithOneSlider,
                totalProgress = 0f,
            )
        }
        updateTotalProgress(0f)
    }

    fun updateAvatarSize(size: Float) {
        _uiState.update {
            uiState.value.copy(avatarSize = size)
        }
    }

    fun updateSegmentStrokeWidth(width: Float) {
        _uiState.update {
            uiState.value.copy(segmentStrokeWidth = width)
        }
    }

    fun updateSegmentGap(size: Float) {
        _uiState.update {
            uiState.value.copy(segmentGap = size)
        }
    }
}

data class MainUiState(
    val progressList: List<Float> = arrayListOf(),
    val totalProgress: Float = 0f,
    val controlAllSegmentsWithOneSlider: Boolean = false,
    val avatarSize: Float = 128f,
    val segmentStrokeWidth: Float = 6f,
    val segmentGap: Float = 12f,
)