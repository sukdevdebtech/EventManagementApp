package com.techexactly.eventmanager.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.data.repository.EventRepository
import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.isUpcoming
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EventFilter { ALL, UPCOMING, PAST }

data class EventListUiState(
    val loading: Boolean = true,
    val events: List<Event> = emptyList(),
    val error: String? = null
)

class EventListViewModel(
    private val repository: EventRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(EventFilter.ALL)

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-shot messages for the UI (snackbars). */
    val messages = _messages.receiveAsFlow()

    val uiState: StateFlow<EventListUiState> = combine(
        repository.observeEvents(), searchQuery, selectedFilter
    ) { result, query, mode ->
        when (result) {
            is Resource.Success -> EventListUiState(loading = false, events = result.data.applyFilters(query, mode))
            is Resource.Error -> EventListUiState(loading = false, error = result.message)
            Resource.Loading -> EventListUiState(loading = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventListUiState())

    fun onSearchChanged(query: String) { searchQuery.value = query }
    fun onFilterChanged(filter: EventFilter) { selectedFilter.value = filter }

    fun deleteEvent(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteEvent(id)) {
                is Resource.Success -> _messages.send("Event deleted")
                is Resource.Error -> _messages.send(result.message)
                Resource.Loading -> Unit
            }
        }
    }

    private fun List<Event>.applyFilters(query: String, mode: EventFilter): List<Event> {
        val now = clock()
        val q = query.trim()
        return filter { event ->
            val matchesFilter = when (mode) {
                EventFilter.ALL -> true
                EventFilter.UPCOMING -> event.isUpcoming(now)
                EventFilter.PAST -> !event.isUpcoming(now)
            }
            val matchesQuery = q.isEmpty() ||
                event.title.contains(q, ignoreCase = true) ||
                event.description.contains(q, ignoreCase = true) ||
                event.location.contains(q, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }
}
