package com.github.david402.githubsearchdemo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.david402.githubsearchdemo.databinding.ActivityMainBinding
import com.github.david402.githubsearchdemo.databinding.SearchItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val searchAdapter = SearchAdapter()

    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(Dispatchers.IO)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.searchResult.adapter = searchAdapter
//        viewModel.searchResult.observe(this) { handleSearchResult(it) }

        // Start with empty query view
        searchAdapter.submitList(emptyList())
        binding.otherResultText.visibility = View.VISIBLE
        binding.searchResult.visibility = View.GONE
        binding.otherResultText.setText(R.string.not_enough_characters)
        binding.searchText.requestFocus()

        binding.searchText.doAfterTextChanged { editable ->
//            binding.progressHorizontal.visibility = View.VISIBLE
            lifecycleScope.launch {
                viewModel.queryChannel.send(editable.toString())
            }
        }

        viewModel.items.observe(this) {
            if (it.isNotEmpty()) {
                binding.otherResultText.visibility = View.GONE
                binding.searchResult.visibility = View.VISIBLE
            } else {
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.empty_result)
            }
            searchAdapter.submitList(it)
        }
        viewModel.dataLoading.observe(this) { isDownloading ->
            if (isDownloading) {
                binding.progressHorizontal.visibility = View.VISIBLE
            } else {
                binding.progressHorizontal.visibility = View.GONE
            }
        }
        viewModel.refresh()
    }

    private fun handleSearchResult(it: SearchResult) {
        when (it) {
            is ValidResult -> {
                binding.otherResultText.visibility = View.GONE
                binding.searchResult.visibility = View.VISIBLE
                searchAdapter.submitList(it.result)
            }
            is ErrorResult -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.search_error)
            }
            is EmptyResult -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.empty_result)
            }
            is EmptyQuery -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.not_enough_characters)
            }
            is RateLimitError -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.search_rate_limit_error)
            }
            is TerminalError -> {
                // Something wen't terribly wrong!
                println("Our Flow terminated unexpectedly, so we're bailing!")
                Toast.makeText(
                    this,
                    "Unexpected error in SearchRepository!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    class SearchAdapter : ListAdapter<GifObject, SearchViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SearchItemBinding.inflate(layoutInflater, parent, false)
            return SearchViewHolder(parent.context, binding)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            holder.bind(holder.context, getItem(position))
            holder.itemView.setOnClickListener( object : View.OnClickListener {
                override fun onClick(v: View?) {
                    Toast.makeText(v?.context, "CLICKED!!!!!", Toast.LENGTH_SHORT).show()
                }

            })
        }

        companion object {
            private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GifObject>() {
                override fun areItemsTheSame(oldItem: GifObject, newItem: GifObject): Boolean =
                    oldItem == newItem

                override fun areContentsTheSame(oldItem: GifObject, newItem: GifObject): Boolean =
                    oldItem == newItem
            }
        }
    }

    class SearchViewHolder(val context: Context, private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, gif: GifObject) {
            Glide
                .with(context)
                .load(gif.images.downsized.url)
                .centerCrop()
                .placeholder(R.drawable.user_profile_placeholder)
                .into(binding.image)
            binding.title.text = gif.title
        }
    }
}