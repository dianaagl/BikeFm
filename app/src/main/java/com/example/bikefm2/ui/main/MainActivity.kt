package com.example.bikefm2.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bikefm2.R
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.model.Friend
import com.example.bikefm2.ui.bottomPanel.FriendAdapter
import com.example.bikefm2.ui.bottomPanel.ModalBottomSheetFragment
import com.example.bikefm2.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
           supportActionBar?.title = result.data?.getStringExtra("displayName")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        mainViewModel = ViewModelProvider(this).get<MainViewModel>(MainViewModel::class.java)
        mainViewModel.getUser()

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            mainViewModel.verifyUser()
            swipeRefreshLayout.isRefreshing = false
        }

        mainViewModel._userRepository.user.observe(this, Observer {
            val loginResult = it ?: return@Observer
            when (loginResult) {
                is Result.Success -> {
                    supportActionBar?.title = loginResult.data.displayName
                    val bottomFragment = supportFragmentManager.findFragmentByTag("bottom_sheet")
                            as ModalBottomSheetFragment
                    bottomFragment.onFriendsAddedCallback(
                        friends = loginResult.data.friends ?: listOf(),
                        sentFriendReqs = loginResult.data.sentFriendsRequests ?: listOf(),
                        incomingFriendReqs = loginResult.data.friendsRequests ?: listOf()
                    )
                }
                is Result.Error -> {
                    startForResult.launch(Intent(this, LoginActivity::class.java))
                }
            }
        })

        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), networkCallback
        )
        val bottomSheet = findViewById<View>(R.id.bottom_sheet)
        val sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.addBottomSheetCallback(
            object : BottomSheetCallback() {
                @SuppressLint("WrongConstant")
                override fun onStateChanged(view: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
//                        btn_bottom_sheet.setText("Close Sheet"
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                        }
                        BottomSheetBehavior.STATE_DRAGGING -> {
                        }
                        BottomSheetBehavior.STATE_SETTLING -> {
                        }
                    }
                }
                override fun onSlide(view: View, v: Float) {}
            })


        val bottomNavigationView =
            findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.friends -> {
//                    val bottomSheetFragment = ModalBottomSheet()
//                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
//                    imgMap.setVisibility(VISIBLE)
//                    imgDial.setVisibility()
//                    imgMail.setVisibility(View.GONE)
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                R.id.page_2 -> {

                }
            }
            false
        }

    }

    private val networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                mainViewModel.verifyUser()
                super.onAvailable(network)
            }

            override fun onLost(network: Network) {

                super.onLost(network)
            }

            override fun onUnavailable() {

                super.onUnavailable()
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_favorite -> {
            runBlocking { mainViewModel.logout().join() }
            mainViewModel.getUser()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
