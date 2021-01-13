package com.example.bikefm2.ui.search

import android.widget.SearchView

class SearchListener: androidx.appcompat.widget.SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }
}