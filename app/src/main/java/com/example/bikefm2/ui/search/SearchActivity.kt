package com.example.bikefm2.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikefm2.R
import com.example.bikefm2.data.Result
import com.example.bikefm2.data.UserRepository
import com.example.bikefm2.data.model.Friend
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class SearchActivity : AppCompatActivity(), SearchView.OnQueryTextListener, SearchAdapter.OnPossibleFriendClickListener{
    @Inject lateinit var userRepository: UserRepository
    private lateinit var listView: RecyclerView
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        setSupportActionBar(findViewById(R.id.my_toolbar))

        listView = findViewById(R.id.listView)

        adapter = SearchAdapter(this)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        listView.layoutManager = llm
        listView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu_activity, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = (menu?.findItem(R.id.search)?.actionView as SearchView)
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Toast.makeText(this, "Searching by: $query", Toast.LENGTH_SHORT).show()
        } else if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.dataString
            Toast.makeText(this, "Suggestion: $uri", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun doMySearch(query: String) {
        lifecycleScope.launch{
            when (val users = userRepository.findUser(query)) {
                is Result.Success -> {
                    adapter.updateFriendList(users.data)
                    adapter.notifyDataSetChanged()
                }
                is Result.Error -> Toast.makeText(this@SearchActivity, users.exception.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query !== null) {
            lifecycleScope.launch {
                doMySearch(query)
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onFriendClick(friend: Friend) {
        lifecycleScope.launch {
            userRepository.addFriend(friend.userId)
        }
        Toast.makeText(this, "added", Toast.LENGTH_SHORT).show()
    }
}