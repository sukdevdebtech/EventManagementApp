package com.techexactly.eventmanager.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techexactly.eventmanager.data.repository.AuthRepository
import com.techexactly.eventmanager.data.repository.EventRepository
import com.techexactly.eventmanager.data.repository.FirebaseAuthRepository
import com.techexactly.eventmanager.data.repository.FirestoreEventRepository
import com.techexactly.eventmanager.ui.auth.AuthViewModel
import com.techexactly.eventmanager.ui.dashboard.DashboardViewModel
import com.techexactly.eventmanager.ui.events.AddEditEventViewModel
import com.techexactly.eventmanager.ui.events.EventListViewModel

/** Minimal manual dependency injection (swap for Hilt/Koin if you prefer). */
object ServiceLocator {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val eventRepository: EventRepository by lazy { FirestoreEventRepository() }
}

class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(ServiceLocator.authRepository)
        modelClass.isAssignableFrom(EventListViewModel::class.java) ->
            EventListViewModel(ServiceLocator.eventRepository)
        modelClass.isAssignableFrom(AddEditEventViewModel::class.java) ->
            AddEditEventViewModel(ServiceLocator.eventRepository)
        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(ServiceLocator.eventRepository)
        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    } as T
}
