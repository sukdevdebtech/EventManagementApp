package com.techexactly.eventmanager.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.databinding.FragmentDashboardBinding
import com.techexactly.eventmanager.di.ViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels { ViewModelFactory() }

    private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupChart()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun setupChart() = with(binding.chart) {
        val onSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface, Color.GRAY)
        description.isEnabled = false
        legend.isEnabled = false
        setScaleEnabled(false)
        setNoDataText(getString(R.string.no_events))
        setNoDataTextColor(onSurface)
        axisRight.isEnabled = false
        axisLeft.apply {
            axisMinimum = 0f
            granularity = 1f
            this.textColor = onSurface
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = value.toInt().toString()
            }
        }
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(months)
            this.textColor = onSurface
        }
    }

    private fun render(state: DashboardUiState) {
        binding.tvTotal.text = state.total.toString()
        binding.tvUpcoming.text = state.upcoming.toString()
        binding.tvPast.text = state.past.toString()
        binding.tvChartTitle.text = getString(R.string.events_per_month, state.year)
        binding.tvError.visibility = if (state.error != null) View.VISIBLE else View.GONE
        binding.tvError.text = state.error

        val entries = state.monthlyCounts.mapIndexed { i, c -> BarEntry(i.toFloat(), c.toFloat()) }
        val textColor = MaterialColors.getColor(binding.chart, com.google.android.material.R.attr.colorOnSurface, Color.GRAY)
        val dataSet = BarDataSet(entries, getString(R.string.events)).apply {
            color = MaterialColors.getColor(binding.chart, com.google.android.material.R.attr.colorPrimary, Color.BLUE)
            valueTextColor = textColor
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) = if (value == 0f) "" else value.toInt().toString()
            }
        }
        binding.chart.data = BarData(dataSet).apply { barWidth = 0.6f }
        binding.chart.animateY(500)
        binding.chart.invalidate()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
