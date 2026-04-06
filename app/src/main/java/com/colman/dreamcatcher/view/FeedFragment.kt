package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colman.dreamcatcher.databinding.FragmentFeedBinding
import com.colman.dreamcatcher.viewmodel.FeedViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState

class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FeedViewModel by activityViewModels()
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

        if (viewModel.posts.value.isNullOrEmpty()) {
            viewModel.loadFirstPage()
        }
    }

    private fun setupRecyclerView() {
        adapter = FeedAdapter()
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 2) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            binding.rvFeed.post {
                if (_binding != null) {
                    adapter.posts = posts
                    binding.tvEmpty.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state == LoadingState.LOADING
        }

        viewModel.isLoadingMore.observe(viewLifecycleOwner) { loading ->
            binding.rvFeed.post {
                if (_binding != null) {
                    adapter.footerState = if (loading) FooterState.LOADING else FooterState.HIDDEN
                }
            }
        }

        viewModel.isEndReached.observe(viewLifecycleOwner) { ended ->
            if (ended) {
                binding.rvFeed.post {
                    if (_binding != null) {
                        adapter.footerState = FooterState.END
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadFirstPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
