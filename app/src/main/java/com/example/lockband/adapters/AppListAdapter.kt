package com.example.lockband.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lockband.R
import kotlinx.android.synthetic.main.app_list_element.view.*

class AppListAdapter(private val data: MutableList<ApplicationInfo>, private val context: Context) :
    RecyclerView.Adapter<AppListAdapter.AppListViewHolder>() {

    inner class AppListViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.app_list_element_icon
        val name: TextView = view.app_list_element_name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppListViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.app_list_element, parent, false)
        return AppListViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppListViewHolder, position: Int) {
        val item = data[position]
        val icon: Drawable = item.loadIcon(context.packageManager)

        holder.icon.setImageDrawable(icon)
        holder.name.text = item.loadLabel(context.packageManager)

    }

    override fun getItemCount(): Int = data.size

}