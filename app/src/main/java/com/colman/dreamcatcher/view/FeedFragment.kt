package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.dreamcatcher.databinding.FragmentFeedBinding
import com.colman.dreamcatcher.viewmodel.FeedViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPosts()
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter()
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.posts = posts
            binding.tvEmpty.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state == LoadingState.LOADING
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
