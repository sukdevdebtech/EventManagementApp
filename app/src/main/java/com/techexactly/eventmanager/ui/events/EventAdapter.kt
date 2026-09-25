package com.techexactly.eventmanager.ui.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.techexactly.eventmanager.R
import com.techexactly.eventmanager.data.model.Event
import com.techexactly.eventmanager.databinding.ItemEventBinding
import com.techexactly.eventmanager.util.DateUtils
import com.techexactly.eventmanager.util.isUpcoming
import com.techexactly.eventmanager.util.timeMillis

class EventAdapter(
    private val onEdit: (Event) -> Unit,
    private val onDelete: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.ViewHolder>(Diff) {

    inner class ViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) = with(binding) {
            tvTitle.text = event.title
            tvDate.text = DateUtils.format(event.timeMillis)
            tvLocation.text = event.location
            tvLocation.visibility = if (event.location.isBlank()) View.GONE else View.VISIBLE
            tvDescription.text = event.description
            tvDescription.visibility = if (event.description.isBlank()) View.GONE else View.VISIBLE

            val upcoming = event.isUpcoming(System.currentTimeMillis())
            tvStatus.setText(if (upcoming) R.string.status_upcoming else R.string.status_past)
            tvStatus.setTextColor(
                ContextCompat.getColor(root.context, if (upcoming) R.color.status_upcoming else R.color.status_past)
            )
            btnEdit.setOnClickListener { onEdit(event) }
            btnDelete.setOnClickListener { onDelete(event) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    private object Diff : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(old: Event, new: Event) = old.id == new.id
        override fun areContentsTheSame(old: Event, new: Event) = old == new
    }
}
