package com.techexactly.eventmanager.ui

import com.techexactly.eventmanager.MainDispatcherRule
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.fakes.FakeEventRepository
import com.techexactly.eventmanager.ui.events.EventFilter
import com.techexactly.eventmanager.ui.events.EventListViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class EventListViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val now = 1_800_000_000_000L
    private val hour = 3_600_000L
    private lateinit var repo: FakeEventRepository

    @Before fun setUp() { repo = FakeEventRepository() }

    private fun event(title: String, offsetMillis: Long, location: String = "") =
        Event(title = title, location = location, dateTime = Timestamp(Date(now + offsetMillis)))

    private suspend fun seed() {
        repo.addEvent(event("Old meetup", -48 * hour, "Kolkata"))
        repo.addEvent(event("Team lunch", 2 * hour, "Cafe"))
        repo.addEvent(event("Launch party", 24 * hour, "Office"))
    }

    private fun kotlinx.coroutines.test.TestScope.collect(vm: EventListViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
    }

    @Test fun `events are shown in reverse chronological order`() = runTest {
        seed()
        val vm = EventListViewModel(repo) { now }
        collect(vm)
        assertEquals(listOf("Launch party", "Team lunch", "Old meetup"), vm.uiState.value.events.map { it.title })
    }

    @Test fun `upcoming and past filters work`() = runTest {
        seed()
        val vm = EventListViewModel(repo) { now }
        collect(vm)

        vm.onFilterChanged(EventFilter.UPCOMING)
        assertEquals(listOf("Launch party", "Team lunch"), vm.uiState.value.events.map { it.title })

        vm.onFilterChanged(EventFilter.PAST)
        assertEquals(listOf("Old meetup"), vm.uiState.value.events.map { it.title })
    }

    @Test fun `search matches title and location ignoring case`() = runTest {
        seed()
        val vm = EventListViewModel(repo) { now }
        collect(vm)

        vm.onSearchChanged("LUNCH")
        assertEquals(listOf("Team lunch"), vm.uiState.value.events.map { it.title })

        vm.onSearchChanged("kolkata")
        assertEquals(listOf("Old meetup"), vm.uiState.value.events.map { it.title })
    }

    @Test fun `deleting an event removes it from the list`() = runTest {
        seed()
        val vm = EventListViewModel(repo) { now }
        collect(vm)

        val id = vm.uiState.value.events.first { it.title == "Team lunch" }.id
        vm.deleteEvent(id)

        assertEquals(listOf("Launch party", "Old meetup"), vm.uiState.value.events.map { it.title })
    }
}
