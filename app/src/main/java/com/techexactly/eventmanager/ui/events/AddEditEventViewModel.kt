package com.techexactly.eventmanager.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.data.repository.EventRepository
import com.techexactly.eventmanager.util.Resource
import com.techexactly.eventmanager.util.Validators
import com.techexactly.eventmanager.util.timeMillis
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date

data class AddEditUiState(
    val loading: Boolean = false,
    val titleError: String? = null,
    val dateError: String? = null,
    val errorMessage: String? = null,
    val saved: Boolean = false
)

class AddEditEventViewModel(
    private val repository: EventRepository,
    private val clock: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditUiState())
    val state: StateFlow<AddEditUiState> = _state.asStateFlow()

    private val _dateTime = MutableStateFlow<Long?>(null)
    /** Selected date & time in epoch millis. Kept here so it survives rotation. */
    val dateTime: StateFlow<Long?> = _dateTime.asStateFlow()

    private val _prefill = Channel<Event>(Channel.CONFLATED)
    /** Emits the existing event once when editing, so the screen can fill its fields. */
    val prefill = _prefill.receiveAsFlow()

    private var editingEvent: Event? = null
    private var loadRequested = false

    val isEditing: Boolean get() = editingEvent != null

    fun load(eventId: String?) {
        if (eventId == null || loadRequested) return
        loadRequested = true
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            when (val result = repository.getEvent(eventId)) {
                is Resource.Success -> {
                    editingEvent = result.data
                    _dateTime.value = result.data.timeMillis
                    _prefill.send(result.data)
                    _state.value = _state.value.copy(loading = false)
                }
                is Resource.Error -> _state.value = _state.value.copy(loading = false, errorMessage = result.message)
                Resource.Loading -> Unit
            }
        }
    }

    fun setDateTime(millis: Long) {
        _dateTime.value = millis
        _state.value = _state.value.copy(dateError = null)
    }

    fun save(title: String, description: String, location: String) {
        val titleError = Validators.validateTitle(title)
        val dateError = Validators.validateEventDate(_dateTime.value, clock(), editingEvent?.timeMillis)
        if (titleError != null || dateError != null) {
            _state.value = _state.value.copy(titleError = titleError, dateError = dateError)
            return
        }

        val timestamp = Timestamp(Date(_dateTime.value!!))
        val existing = editingEvent
        val event = existing?.copy(
            title = title.trim(), description = description.trim(),
            location = location.trim(), dateTime = timestamp
        ) ?: Event(
            title = title.trim(), description = description.trim(),
            location = location.trim(), dateTime = timestamp
        )

        viewModelScope.launch {
            _state.value = AddEditUiState(loading = true)
            val result = if (existing == null) repository.addEvent(event) else repository.updateEvent(event)
            _state.value = when (result) {
                is Resource.Success -> AddEditUiState(saved = true)
                is Resource.Error -> AddEditUiState(errorMessage = result.message)
                Resource.Loading -> AddEditUiState(loading = true)
            }
        }
    }

    fun onErrorShown() { _state.value = _state.value.copy(errorMessage = null) }
}
