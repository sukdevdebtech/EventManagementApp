package com.techexactly.eventmanager.ui.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.databinding.FragmentEventListBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class EventListFragment : Fragment() {

    private var _binding: FragmentEventListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventListViewModel by viewModels { ViewModelFactory() }

    private val adapter = EventAdapter(
        onEdit = { event ->
            startActivity(
                Intent(requireContext(), AddEditEventActivity::class.java)
                    .putExtra(AddEditEventActivity.EXTRA_EVENT_ID, event.id)
            )
        },
        onDelete = ::confirmDelete
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEventListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recycler.adapter = adapter
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditEventActivity::class.java))
        }
        binding.etSearch.doAfterTextChanged { viewModel.onSearchChanged(it?.toString().orEmpty()) }
        binding.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            val filter = when (ids.firstOrNull()) {
                R.id.chip_upcoming -> EventFilter.UPCOMING
                R.id.chip_past -> EventFilter.PAST
                else -> EventFilter.ALL
            }
            viewModel.onFilterChanged(filter)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch {
                    viewModel.messages.collect { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private fun render(state: EventListUiState) {
        binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
        adapter.submitList(state.events)
        binding.tvEmpty.visibility =
            if (!state.loading && state.events.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmpty.text = state.error ?: getString(R.string.no_events)
    }

    private fun confirmDelete(event: Event) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_event)
            .setMessage(getString(R.string.delete_confirm, event.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteEvent(event.id) }
            .show()
    }

    override fun onDestroyView() {
        binding.recycler.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
