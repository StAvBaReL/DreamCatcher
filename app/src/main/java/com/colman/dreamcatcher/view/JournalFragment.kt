package com.colman.dreamcatcher.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.dreamcatcher.R
import com.colman.dreamcatcher.databinding.FragmentJournalBinding
import com.colman.dreamcatcher.viewmodel.JournalViewModel
import com.colman.dreamcatcher.viewmodel.LoadingState
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: JournalViewModel by viewModels()
    private lateinit var adapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
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
        adapter = JournalAdapter()
        adapter.onEditClick = { post ->
            val action =
                JournalFragmentDirections.actionJournalFragmentToEditDreamFragment(post.postId)
            findNavController().navigate(action)
        }
        adapter.onDeleteClick = { post ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_dialog_message)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    viewModel.deletePost(post.postId) {}
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
        binding.rvJournal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJournal.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.posts = posts.toMutableList()
            adapter.notifyDataSetChanged()
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
