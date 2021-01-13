package com.example.bikefm2.ui.search

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikefm2.R
import com.example.bikefm2.data.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.time.Duration


@AndroidEntryPoint
class SearchActivity : AppCompatActivity(){
    @Inject lateinit var _userRepository: UserRepository
    private lateinit var listView: RecyclerView
    private lateinit var adapter: SearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.my_toolbar))

//        listView = findViewById<RecyclerView>(R.id.listView)
//
//        adapter = SearchAdapter()
//        listView.adapter = adapter
//        if (Intent.ACTION_SEARCH == intent.action) {
//            intent.getStringExtra(SearchManager.QUERY)?.also { query ->
//                lifecycleScope.launch {
//                    doMySearch(query)
//                }
//            }
//        }
        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Toast.makeText(this, "Searching by: $query", Toast.LENGTH_LONG).show();
        } else if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.dataString
            Toast.makeText(this, "Suggestion: $uri", Toast.LENGTH_LONG).show();
        }
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
        searchView.setOnQueryTextListener(
            SearchListener()
        )
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
       val users =  _userRepository.findUser(query)
       adapter.swapData(users)
        adapter.notifyDataSetChanged()
    }
}