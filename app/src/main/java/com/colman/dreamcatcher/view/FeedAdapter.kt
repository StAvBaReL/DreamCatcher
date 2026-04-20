package com.colman.dreamcatcher.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.colman.dreamcatcher.databinding.FeedPostRowBinding
import com.colman.dreamcatcher.model.DreamPost

class FeedAdapter : PagingDataAdapter<DreamPost, FeedViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val binding =
            FeedPostRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val post = getItem(position)
        if (post != null) {
            holder.bind(post)
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<DreamPost>() {
        override fun areItemsTheSame(oldPost: DreamPost, newPost: DreamPost): Boolean {
            return oldPost.postId == newPost.postId
        }

        override fun areContentsTheSame(oldPost: DreamPost, newPost: DreamPost): Boolean {
            return oldPost == newPost
        }
    }
}
