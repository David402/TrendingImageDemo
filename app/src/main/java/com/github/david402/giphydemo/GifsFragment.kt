package com.github.david402.giphydemo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.david402.giphydemo.data.GifObject
import com.github.david402.giphydemo.databinding.SearchItemBinding
import com.github.david402.giphydemo.databinding.TrendingGifFragBinding
import com.github.david402.giphydemo.util.getViewModelFactory
import kotlinx.coroutines.launch

class GifsFragment : Fragment() {
    private val viewModel by viewModels<SearchViewModel> { getViewModelFactory() }

    private val args : GifsFragmentArgs by navArgs()
    private lateinit var binding : TrendingGifFragBinding
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TrendingGifFragBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        setHasOptionsMenu(true)
        viewModel.searchResult.observe(this) {}

        // Start with empty query view
        binding.otherResultText.visibility = View.VISIBLE
        binding.searchResult.visibility = View.GONE
        binding.otherResultText.setText(R.string.not_enough_characters)
        binding.searchText.requestFocus()

        binding.searchText.doAfterTextChanged { editable ->
            lifecycleScope.launch {
                viewModel.queryChannel.send(editable.toString())
            }
        }

        // Handle items changed
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

        // Handle loading progress indicator changed
        viewModel.dataLoading.observe(this) { isDownloading ->
            if (isDownloading) {
                binding.progressHorizontal.visibility = View.VISIBLE
            } else {
                binding.progressHorizontal.visibility = View.GONE
            }
        }
        viewModel.refresh()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        setupNavigation()
        setupListAdapter()
    }

    private fun setupListAdapter() {
        val viewModel = binding.viewmodel
        if (viewModel != null) {
            searchAdapter = SearchAdapter(viewModel)
            binding.searchResult.adapter = searchAdapter
        }
    }

    private fun setupNavigation() {
        viewModel.openGifEvent.observe(viewLifecycleOwner, EventObserver {
            openGifDetails(it)
        })
    }

    private fun openGifDetails(id: String) {
        val action = GifsFragmentDirections.actionGifsFragmentToGifDetailsFragment(id)
        findNavController().navigate(action)
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
                    context,
                    "Unexpected error in GiphyRepository!",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }

    class SearchAdapter(private val viewModel: SearchViewModel) : ListAdapter<GifObject, SearchViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = SearchItemBinding.inflate(layoutInflater, parent, false)
            return SearchViewHolder(parent.context, binding)
        }

        override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
            holder.bind(holder.context, getItem(position), viewModel)
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
        fun bind(context: Context, gif: GifObject, viewModel: SearchViewModel) {
            Glide
                .with(context)
                .load(gif.images.downsized.url)
                .centerCrop()
                .placeholder(R.drawable.user_profile_placeholder)
                .into(binding.image)
            binding.title.text = gif.title
            binding.viewmodel = viewModel
            binding.gif = gif
        }
    }
}