package com.colman.dreamcatcher.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.colman.dreamcatcher.databinding.FeedFooterBinding

class FeedFooterViewHolder(private val binding: FeedFooterBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(state: FooterState) {
        binding.pbLoadMore.visibility = if (state == FooterState.LOADING) View.VISIBLE else View.GONE
        binding.tvEnd.visibility = if (state == FooterState.END) View.VISIBLE else View.GONE
    }
}
