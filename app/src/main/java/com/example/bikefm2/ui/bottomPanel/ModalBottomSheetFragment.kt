package com.example.bikefm2.ui.bottomPanel

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bikefm2.R
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.data.model.FriendTypes
import com.example.bikefm2.ui.login.LoginViewModel
import com.example.bikefm2.ui.search.SearchActivity
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_modal_bottomsheet.*

/**
 * A simple [Fragment] subclass.
 * Use the [ModalBottomSheet.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ModalBottomSheetFragment : Fragment(), FriendAdapter.FriendItemListener {
    private var showFriendsState: FriendTypes = FriendTypes.friendReqs
    private lateinit var friendsAdapter: FriendAdapter
    private lateinit var bottomSheetViewModel: BottomSheetViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_modal_bottomsheet, container, false)

        bottomSheetViewModel = ViewModelProvider(this).get<BottomSheetViewModel>(BottomSheetViewModel::class.java)
        bottomSheetViewModel.user.observe(viewLifecycleOwner, Observer {

        })


        friendsAdapter = FriendAdapter(requireContext(), FriendTypes.friends)

        val listView = view.findViewById<RecyclerView>(R.id.friends_list)
        listView.adapter = friendsAdapter

        val searchFriendButton = view.findViewById<MaterialButton>(R.id.addFriendButton)
        val showFriendsButton = view.findViewById<MaterialButton>(R.id.showFriends)
        val showFriendRequestsButton = view.findViewById<MaterialButton>(R.id.showFriendRequests)
        val showSentFriendRequestsButton = view.findViewById<MaterialButton>(R.id.showSentFriendRequests)

        searchFriendButton.setOnClickListener {
            val intent = Intent(activity, SearchActivity::class.java).apply {
            }
            startActivity(intent)
        }

        showFriendsButton.setOnClickListener{
            showFriendsState = FriendTypes.friends
            friendsAdapter.updateFriends(bottomSheetViewModel.friends, showFriendsState)
        }
        showFriendRequestsButton.setOnClickListener{
            showFriendsState = FriendTypes.friendReqs
            friendsAdapter.updateFriends(bottomSheetViewModel.sentFriendReqs, showFriendsState)
        }
        showSentFriendRequestsButton.setOnClickListener{
            showFriendsState = FriendTypes.incomingFriendReq
            friendsAdapter.updateFriends(bottomSheetViewModel.incomingFriendReqs, showFriendsState)
        }
        return view
    }

    fun onFriendsAddedCallback(friends: List<Friend>, sentFriendReqs: List<Friend>, incomingFriendReqs: List<Friend>) {
        bottomSheetViewModel.friends = friends
        bottomSheetViewModel.sentFriendReqs = sentFriendReqs
        bottomSheetViewModel.incomingFriendReqs = incomingFriendReqs
    }

    override fun onItemClick(friend: Friend) {

    }
}
