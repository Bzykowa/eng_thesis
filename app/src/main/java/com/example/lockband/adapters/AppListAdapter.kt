package com.example.lockband.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.data.room.entities.AppState
import com.example.lockband.databinding.AppListElementBinding

class AppListAdapter(val listener: (Any) -> Unit, private val icons : MutableList<Drawable>) : ListAdapter<AppState, RecyclerView.ViewHolder>(AppStateDiffCallback()) {

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
        (holder as AppListViewHolder).bind(getItem(position), icons[position])
    }


    inner class AppListViewHolder(private val binding: AppListElementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppState, icon : Drawable) {
            binding.apply {
                app = item
                lockSwitch.setOnCheckedChangeListener { _, isChecked ->
                    listener(AppState(item.packageName,item.label,isChecked))
                }
                appListElementIcon.setImageDrawable(icon)
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