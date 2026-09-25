package com.techexactly.eventmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.data.repository.EventRepository
import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.timeMillis
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DashboardUiState(
    val loading: Boolean = true,
    val total: Int = 0,
    val upcoming: Int = 0,
    val past: Int = 0,
    val year: Int = 0,
    /** Index 0 = January ... 11 = December, for [year]. */
    val monthlyCounts: List<Int> = List(12) { 0 },
    val error: String? = null
)

class DashboardViewModel(
    repository: EventRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = repository.observeEvents()
        .map { result ->
            when (result) {
                is Resource.Success -> buildStats(result.data)
                is Resource.Error -> DashboardUiState(loading = false, error = result.message)
                Resource.Loading -> DashboardUiState(loading = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    private fun buildStats(events: List<Event>): DashboardUiState {
        val now = clock()
        val year = Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.YEAR)
        val counts = MutableList(12) { 0 }
        val cal = Calendar.getInstance()
        events.forEach { event ->
            cal.timeInMillis = event.timeMillis
            if (cal.get(Calendar.YEAR) == year) counts[cal.get(Calendar.MONTH)]++
        }
        val upcoming = events.count { it.timeMillis >= now }
        return DashboardUiState(
            loading = false,
            total = events.size,
            upcoming = upcoming,
            past = events.size - upcoming,
            year = year,
            monthlyCounts = counts
        )
    }
}
