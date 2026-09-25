package com.techexactly.eventmanager.ui

import com.techexactly.eventmanager.MainDispatcherRule
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.fakes.FakeEventRepository
import com.techexactly.eventmanager.ui.dashboard.DashboardViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun millis(year: Int, month: Int, day: Int) =
        Calendar.getInstance().apply { clear(); set(year, month, day, 12, 0, 0) }.timeInMillis

    @Test fun `counts total upcoming past and events per month`() = runTest {
        val now = millis(2026, Calendar.SEPTEMBER, 15)
        val repo = FakeEventRepository()
        listOf(
            millis(2026, Calendar.JANUARY, 10),    // past
            millis(2026, Calendar.SEPTEMBER, 20),  // upcoming
            millis(2026, Calendar.SEPTEMBER, 25),  // upcoming
            millis(2027, Calendar.MARCH, 5)        // upcoming, other year -> not in chart
        ).forEachIndexed { i, t ->
            repo.addEvent(Event(title = "E$i", dateTime = Timestamp(Date(t))))
        }

        val vm = DashboardViewModel(repo) { now }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val state = vm.uiState.value
        assertEquals(4, state.total)
        assertEquals(3, state.upcoming)
        assertEquals(1, state.past)
        assertEquals(2026, state.year)
        assertEquals(1, state.monthlyCounts[Calendar.JANUARY])
        assertEquals(2, state.monthlyCounts[Calendar.SEPTEMBER])
        assertEquals(0, state.monthlyCounts[Calendar.MARCH])
    }
}
