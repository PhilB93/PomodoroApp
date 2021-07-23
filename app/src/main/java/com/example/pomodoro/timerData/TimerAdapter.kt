package com.example.pomodoro.timerData

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.pomodoro.databinding.TimerItemBinding

class TimerAdapter(
    private val listener: TimerListener
) : ListAdapter<Timer, TimerViewHolder>(itemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = TimerItemBinding.inflate(layoutInflater, parent, false)
        return TimerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) =
        holder.bind(getItem(position))

    private companion object {
        private val itemComparator = object : DiffUtil.ItemCallback<Timer>() {
            override fun areItemsTheSame(oldItem: Timer, newItem: Timer): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Timer, newItem: Timer): Boolean =
                oldItem.leftMS == newItem.leftMS && oldItem.isStarted == newItem.isStarted

            override fun getChangePayload(oldItem: Timer, newItem: Timer) = Any()
        }
    }

    object Start {
        var startedPosition = -1
    }
}