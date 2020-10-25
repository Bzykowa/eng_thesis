package com.example.lockband.adapters

import android.content.pm.ApplicationInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.data.AppState
import com.example.lockband.databinding.AppListElementBinding

class AppListAdapter : ListAdapter<AppState, RecyclerView.ViewHolder>(AppStateDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return AppListViewHolder(
            AppListElementBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val app = getItem(position)
        (holder as AppListViewHolder).bind(app)
    }


    inner class AppListViewHolder(private val binding: AppListElementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppState) {
            binding.apply {
                app = item
                executePendingBindings()
            }
        }
    }
}

private class AppStateDiffCallback : DiffUtil.ItemCallback<AppState>() {
    override fun areItemsTheSame(oldItem: AppState, newItem: AppState): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: AppState, newItem: AppState): Boolean {
        return oldItem == newItem
    }
}