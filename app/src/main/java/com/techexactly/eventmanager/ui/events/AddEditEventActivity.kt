package com.techexactly.eventmanager.ui.events

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.databinding.ActivityAddEditEventBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.techexactly.eventmanager.util.DateUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class AddEditEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEventBinding
    private val viewModel: AddEditEventViewModel by viewModels { ViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(if (eventId == null) R.string.add_event else R.string.edit_event)

        binding.etDateTime.setOnClickListener { pickDateTime() }
        binding.btnSave.setOnClickListener {
            viewModel.save(
                binding.etTitle.text.toString(),
                binding.etDescription.text.toString(),
                binding.etLocation.text.toString()
            )
        }

        viewModel.load(eventId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.state.collect(::render) }
                launch {
                    viewModel.dateTime.collect { millis ->
                        binding.etDateTime.setText(millis?.let { DateUtils.format(it) }.orEmpty())
                    }
                }
                launch {
                    viewModel.prefill.collect { event ->
                        binding.etTitle.setText(event.title)
                        binding.etDescription.setText(event.description)
                        binding.etLocation.setText(event.location)
                    }
                }
            }
        }
    }

    private fun render(state: AddEditUiState) {
        binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !state.loading
        binding.tilTitle.error = state.titleError
        binding.tilDateTime.error = state.dateError
        state.errorMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            viewModel.onErrorShown()
        }
        if (state.saved) finish()
    }

    /** Step 1: pick a date. Step 2: pick a time. */
    private fun pickDateTime() {
        val current = Calendar.getInstance().apply { viewModel.dateTime.value?.let { timeInMillis = it } }

        // MaterialDatePicker works in UTC midnight - convert to/from the local date.
        val initialUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.select_date)
            .setSelection(initialUtc)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = selection }
            val year = utc.get(Calendar.YEAR)
            val month = utc.get(Calendar.MONTH)
            val day = utc.get(Calendar.DAY_OF_MONTH)

            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(current.get(Calendar.HOUR_OF_DAY))
                .setMinute(current.get(Calendar.MINUTE))
                .setTitleText(R.string.select_time)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val result = Calendar.getInstance().apply {
                    set(year, month, day, timePicker.hour, timePicker.minute, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                viewModel.setDateTime(result.timeInMillis)
            }
            timePicker.show(supportFragmentManager, "time_picker")
        }
        datePicker.show(supportFragmentManager, "date_picker")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_EVENT_ID = "event_id"
    }
}
