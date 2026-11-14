package com.rawkit.launcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rawkit.launcher.R
import com.rawkit.launcher.data.AppEntry

class AppGridAdapter(
    private val onClick: (AppEntry) -> Unit,
    private val onLongClick: (AppEntry) -> Unit,
) : ListAdapter<AppEntry, AppGridAdapter.AppViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.app_icon)
        private val label: TextView = itemView.findViewById(R.id.app_label)

        fun bind(entry: AppEntry) {
            icon.setImageDrawable(entry.icon)
            label.text = entry.label
            itemView.setOnClickListener { onClick(entry) }
            itemView.setOnLongClickListener {
                onLongClick(entry)
                true
            }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<AppEntry>() {
            override fun areItemsTheSame(oldItem: AppEntry, newItem: AppEntry): Boolean =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: AppEntry, newItem: AppEntry): Boolean =
                oldItem.label == newItem.label && oldItem.icon == newItem.icon
        }
    }
}
