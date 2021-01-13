package com.example.bikefm2.data.model

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.bikefm2.R

class FriendAdapter():
    RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(){
    private var friendsList = listOf<Friend>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_layout, parent, false)
        return FriendViewHolder(view)
    }


    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) =
        holder.bind(friendsList.get(position))

    fun updateFriendList( friendsList: List<Friend>){
        this.friendsList = friendsList
    }
    inner class FriendViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private var current: Friend? = null
        private val nameTextView = itemView.findViewById<TextView>(R.id.displayName)
        private val lastnameTextView = itemView.findViewById<TextView>(R.id.lastName)
        init {
            itemView.setOnClickListener{

            }
        }

        fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = friendsList.get(adapterPosition)
        }

        fun bind(item: Friend) = with(itemView) {
            current = item
            nameTextView.text = item.displayName
            lastnameTextView.text = item.lastname
        }
    }

    private class FriendDC : DiffUtil.ItemCallback<Friend>() {
        override fun areItemsTheSame(
            oldItem: Friend,
            newItem: Friend
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: Friend,
            newItem: Friend
        ): Boolean {
            return oldItem.lastname == newItem.lastname &&
                    oldItem.displayName == newItem.displayName
        }
    }

    override fun getItemCount() = friendsList.size

}