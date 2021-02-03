package com.example.bikefm2.ui.search

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.TextView
import com.example.bikefm2.R
import com.example.bikefm2.data.model.Friend
import com.google.android.material.button.MaterialButton

class SearchAdapter(val itemListener: OnPossibleFriendClickListener) :
    RecyclerView.Adapter<SearchAdapter.FriendViewHolder>() {
    var friendsList = listOf<Friend>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapter.FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_friend_layout, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) =
        holder.bind(friendsList.get(position))

    fun updateFriendList( friendsList: List<Friend>){
        this.friendsList = friendsList
    }

    inner class FriendViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {
        private var current: Friend? = null
        private val nameTextView = itemView.findViewById<TextView>(R.id.friend_name)
        private val button = itemView.findViewById<MaterialButton>(R.id.addFriendButton)

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = friendsList.get(adapterPosition)
        }

        fun bind(item: Friend) = with(itemView) {
            current = item
            nameTextView.text = item.displayName
            itemView.findViewById<MaterialButton>(R.id.addFriendButton).setOnClickListener{
                itemListener.onFriendClick(item)
            }
        }
    }


    override fun getItemCount() = friendsList.size

    interface OnPossibleFriendClickListener {
        fun onFriendClick(friend: Friend)
    }
}