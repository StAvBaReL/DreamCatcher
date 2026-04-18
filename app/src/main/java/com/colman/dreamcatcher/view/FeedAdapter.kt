package com.colman.dreamcatcher.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.colman.dreamcatcher.databinding.FeedFooterBinding
import com.colman.dreamcatcher.databinding.FeedPostRowBinding
import com.colman.dreamcatcher.model.DreamPost

enum class FooterState { HIDDEN, LOADING, END }

class FeedAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_POST = 0
        private const val TYPE_FOOTER = 1
    }

    var currentUserId: String = ""
    var onLikeClick: ((DreamPost) -> Unit)? = null

    var posts: List<DreamPost> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var footerState: FooterState = FooterState.HIDDEN
        set(value) {
            field = value
            notifyItemChanged(posts.size)
        }

    override fun getItemCount(): Int = posts.size + 1

    override fun getItemViewType(position: Int): Int =
        if (position == posts.size) TYPE_FOOTER else TYPE_POST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FOOTER) {
            val binding =
                FeedFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            FeedFooterViewHolder(binding)
        } else {
            val binding =
                FeedPostRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            FeedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedFooterViewHolder) {
            holder.bind(footerState)
        } else if (holder is FeedViewHolder) {
            holder.bind(posts[position], currentUserId, onLikeClick)
        }
    }
}
