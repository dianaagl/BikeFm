package com.example.bikefm2.ui.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bikefm2.R
import com.example.bikefm2.data.model.Friend

class SearchAdapter(private val interaction: Interaction? = null) :
    ListAdapter<Friend, SearchAdapter.FriendViewHolder>(FriendDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FriendViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.search_friend_layout, parent, false), interaction
    )

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun swapData(data: List<Friend>) {
        submitList(data.toMutableList())
    }

    inner class FriendViewHolder(
        itemView: View,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
        }

        fun bind(item: Friend) = with(itemView) {
            // TODO: Bind the data with View
        }
    }

    interface Interaction {

    }

    private class FriendDC : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(
            oldItem: Friend,
            newItem: Friend
        ): Boolean {
            TODO(
                "not implemented"
            )
        }

        override fun areContentsTheSame(
            oldItem: Friend,
            newItem: Friend
        ): Boolean {
            TODO(
                "not implemented"
            )
        }
    }
}