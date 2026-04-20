package com.colman.dreamcatcher.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.colman.dreamcatcher.databinding.JournalPostRowBinding
import com.colman.dreamcatcher.model.DreamPost

class JournalAdapter : PagingDataAdapter<DreamPost, JournalViewHolder>(PostDiffCallback()) {

    var onEditClick: ((DreamPost) -> Unit)? = null
    var onDeleteClick: ((DreamPost) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding =
            JournalPostRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JournalViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
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
