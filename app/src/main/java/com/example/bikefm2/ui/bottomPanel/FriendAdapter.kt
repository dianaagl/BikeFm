package com.example.bikefm2.ui.bottomPanel

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.example.bikefm2.R
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.FriendTypes
import com.google.android.material.button.MaterialButton
import org.w3c.dom.Text

class FriendAdapter(val context: Context, var friendState: FriendTypes):
    RecyclerView.Adapter<FriendAdapter.ViewHolder>()
{
    var friends: List<Friend> = listOf()
    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val displayNameText = itemView.findViewById<TextView>(R.id.displayName)
        val showFriends = itemView.findViewById<Switch>(R.id.showSwitch)
        val friendImage = itemView.findViewById<ImageView>(R.id.picture)
        val addFriendButton = itemView.findViewById<MaterialButton>(R.id.addFriendButton)
        val deleteFriendButton = itemView.findViewById<MaterialButton>(R.id.deleteFriendButton)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_friend_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        with(holder){
            when(friendState){
                FriendTypes.friends -> {
                    displayNameText.text = friend.displayName
                    addFriendButton.visibility = View.INVISIBLE
                    showFriends.visibility = View.VISIBLE
                }
                FriendTypes.friendReqs ->  {
                    displayNameText.text = friend.displayName
                    addFriendButton.visibility = View.VISIBLE
                    showFriends.visibility = View.INVISIBLE
                }
                    FriendTypes.incomingFriendReq -> {
                        displayNameText.text = friend.displayName
                        addFriendButton.visibility = View.VISIBLE
                        showFriends.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun updateFriends(updatedList: List<Friend>, friendState: FriendTypes){
        this.friendState = friendState
        friends = updatedList
        notifyDataSetChanged()
    }

    override fun getItemCount() = friends.size

    interface FriendItemListener{
        fun onItemClick(friend: Friend)
    }
}