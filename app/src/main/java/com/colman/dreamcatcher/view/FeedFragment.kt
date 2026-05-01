package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.dreamcatcher.databinding.FragmentFeedBinding
import com.colman.dreamcatcher.viewmodel.FeedViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState

class FeedFragment : Fragment() {

    private var binding: FragmentFeedBinding? = null
    private val viewModel: FeedViewModel by activityViewModels()
    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupSwipeRefresh()

        if (viewModel.posts.value == null) {
            viewModel.loadFirstPage()
        }
    }

    private fun setupRecyclerView() {
        val binding = binding ?: return
        adapter = FeedAdapter()
        adapter.currentUserId = viewModel.currentUserId
        adapter.onLikeClick = { post -> viewModel.toggleLike(post) }
        adapter.onEditClick = { post ->
            val action = FeedFragmentDirections.actionFeedFragmentToEditDreamFragment(post.postId)
            findNavController().navigate(action)
        }
        adapter.onDeleteClick = { post ->
            android.app.AlertDialog.Builder(requireContext())
                .setTitle(com.colman.dreamcatcher.R.string.delete_dialog_title)
                .setMessage(com.colman.dreamcatcher.R.string.delete_dialog_message)
                .setPositiveButton(com.colman.dreamcatcher.R.string.confirm) { _, _ ->
                    viewModel.deletePost(post.postId)
                }
                .setNegativeButton(com.colman.dreamcatcher.R.string.cancel, null)
                .show()
        }
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            val currentBinding = binding ?: return@observe
            adapter.submitData(lifecycle, posts)
            currentBinding.tvEmpty.visibility = View.GONE
        }

        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            val currentBinding = binding ?: return@observe
            currentBinding.swipeRefresh.isRefreshing = state == LoadingState.LOADING
        }
    }

    private fun setupSwipeRefresh() {
        val binding = binding ?: return
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadFirstPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
