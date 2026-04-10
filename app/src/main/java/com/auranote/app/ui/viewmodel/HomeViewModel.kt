package com.auranote.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auranote.app.data.model.Recording
import com.auranote.app.data.model.RecordingType
import com.auranote.app.data.repository.RecordingRepository
import com.auranote.app.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recordings: List<Recording> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: RecordingType? = null,
    val searchQuery: String = "",
    val showSearch: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RecordingRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<RecordingType?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _showSearch = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    private val recordingsFlow = combine(_searchQuery, _selectedFilter) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            if (filter == null) repository.getAllRecordings()
            else repository.getRecordingsByType(filter)
        } else {
            repository.searchRecordings(query)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        recordingsFlow,
        _selectedFilter,
        _searchQuery,
        _showSearch,
        _error
    ) { recordings, filter, query, showSearch, error ->
        HomeUiState(
            recordings = recordings,
            isLoading = false,
            error = error,
            selectedFilter = filter,
            searchQuery = query,
            showSearch = showSearch
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun setFilter(type: RecordingType?) {
        _selectedFilter.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSearch() {
        _showSearch.value = !_showSearch.value
        if (!_showSearch.value) _searchQuery.value = ""
    }

    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            try {
                FileUtils.deleteFile(recording.filePath)
                repository.deleteRecording(recording)
            } catch (e: Exception) {
                _error.value = "Failed to delete recording"
            }
        }
    }

    fun toggleFavorite(recording: Recording) {
        viewModelScope.launch {
            repository.setFavorite(recording.id, !recording.isFavorite)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
